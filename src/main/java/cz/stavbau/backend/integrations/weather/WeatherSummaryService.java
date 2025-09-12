// FILE: src/main/java/cz/stavbau/backend/integrations/weather/WeatherSummaryService.java
package cz.stavbau.backend.integrations.weather;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WeatherSummaryService {

    private static final String DEFAULT_TZ = "Europe/Prague";
    private final MeteostatClient meteostat;

    public WeatherSummaryService(MeteostatClient meteostat) {
        this.meteostat = meteostat;
    }

    /** Vrátí strukturované DTO pro daný den a souřadnice. */
    public Mono<Optional<WeatherSummaryDto>> buildDailySummaryDto(double lat, double lon, @Nullable Integer alt, LocalDate date) {
        var dailyMono = meteostat.getDailyPoint(lat, lon, alt, date)
                .onErrorResume(ex -> Mono.just(new MeteostatClient.DailyPointResponse(List.of())));

        var hourlyMono = meteostat.getHourlyPoint(lat, lon, date, DEFAULT_TZ)
                .onErrorResume(ex -> Mono.just(new MeteostatClient.HourlyPointResponse(List.of())));

        return Mono.zip(dailyMono, hourlyMono)
                .map(tuple -> {
                    var dailyResp = tuple.getT1();
                    var hourlyResp = tuple.getT2();

                    MeteostatClient.Day d = (dailyResp.data() == null || dailyResp.data().isEmpty())
                            ? null : dailyResp.data().get(0);  //.getFirst();

                    WeatherSummaryDto.Daily daily = null;
                    if (d != null) {
                        daily = WeatherSummaryDto.Daily.builder()
                                .tminC(d.tmin())
                                .tmaxC(d.tmax())
                                .windMs(d.wspd())
                                .windGustMs(d.wpgt())
                                .prcpMm(d.prcp())
                                .snowMm(d.snow())
                                .presHpa(d.pres())
                                .sunMinutes(d.tsun() == null ? null : d.tsun().intValue())
                                .build();
                    }

                    String label = null;
                    WeatherSummaryDto.Hourly hourly = null;
                    if (hourlyResp != null && hourlyResp.data() != null && !hourlyResp.data().isEmpty()) {
                        var hours = filterDaylight(hourlyResp.data());
                        if (!hours.isEmpty()) {
                            label = classifyDayFromHourly(hours).orElse(null);

                            var histogram = hours.stream()
                                    .map(MeteostatClient.Hour::coco)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                            Integer major = histogram.entrySet().stream()
                                    .max(Map.Entry.comparingByValue())
                                    .map(Map.Entry::getKey).orElse(null);

                            boolean thunder = anyCocoInRange(hours, 25, 27);
                            boolean rain    = anyCocoIn(hours, Set.of(7,8,9,17,18));
                            boolean snow    = anyCocoIn(hours, Set.of(14,15,16,21,22));

                            hourly = WeatherSummaryDto.Hourly.builder()
                                    .majorConditionCode(major)
                                    .histogram(histogram)
                                    .thunder(thunder)
                                    .rain(rain)
                                    .snow(snow)
                                    .build();
                        }
                    }

                    if (daily == null && hourly == null) return Optional.empty();

                    return Optional.of(WeatherSummaryDto.builder()
                            .date(date)
                            .tz(DEFAULT_TZ)
                            .label(label)
                            .daily(daily)
                            .hourly(hourly)
                            .source("meteostat")
                            .build());
                });
    }

    private static List<MeteostatClient.Hour> filterDaylight(List<MeteostatClient.Hour> hours) {
        // Jednoduchý filtr: ponecháme všechny hodiny dne – případně lze omezit 06–20 lokálního času.
        return hours;
    }

    private static Optional<String> classifyDayFromHourly(List<MeteostatClient.Hour> hours) {
        // Velmi jednoduché mapování COCO → label dne podle nejčastějšího výskytu
        var histogram = hours.stream()
                .map(MeteostatClient.Hour::coco)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        var major = histogram.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);

        return major.map(WeatherSummaryService::cocoToLabel);
    }

    private static boolean anyCocoIn(List<MeteostatClient.Hour> hours, Set<Integer> set) {
        return hours.stream().map(MeteostatClient.Hour::coco).filter(Objects::nonNull).anyMatch(set::contains);
    }

    private static boolean anyCocoInRange(List<MeteostatClient.Hour> hours, int from, int toInclusive) {
        return hours.stream().map(MeteostatClient.Hour::coco).filter(Objects::nonNull).anyMatch(c -> c >= from && c <= toInclusive);
    }

    private static String cocoToLabel(int coco) {
        return switch (coco) {
            case 1 -> "Jasno";
            case 2, 3 -> "Polojasno";
            case 4, 5, 6 -> "Oblačno";
            case 7, 8, 9, 17, 18 -> "Déšť";
            case 10, 11, 12, 13 -> "Přeháňky";
            case 14, 15, 16, 21, 22 -> "Sněžení";
            case 25, 26, 27 -> "Bouřky";
            default -> "Zataženo";
        };
    }
}
