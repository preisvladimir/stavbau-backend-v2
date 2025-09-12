// FILE: src/main/java/cz/stavbau/backend/integrations/weather/MeteostatClient.java
package cz.stavbau.backend.integrations.weather;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Component
public class MeteostatClient {

    private static final String UNITS = "scientific";
    private final WebClient client;

    public MeteostatClient(@Qualifier("meteostatWebClient") WebClient client) {
        this.client = client;
    }

    /** RapidAPI: GET /point/daily?lat=&lon=&alt=&start=YYYY-MM-DD&end=YYYY-MM-DD&units=scientific */
    public Mono<DailyPointResponse> getDailyPoint(double lat, double lon, @Nullable Integer alt, LocalDate date) {
        return client.get()
                .uri(b -> URI.create(buildDailyPointUri(b, lat, lon, alt, date)))
                .retrieve()
                .bodyToMono(DailyPointResponse.class);
    }

    private String buildDailyPointUri(UriBuilder b, double lat, double lon, @Nullable Integer alt, LocalDate date) {
        var ub = b.path("/point/daily")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("start", date)
                .queryParam("end", date)
                .queryParam("units", UNITS);
        if (alt != null) ub.queryParam("alt", alt);
        return ub.build().toString();
    }

    /** RapidAPI: GET /point/hourly?lat=&lon=&start=YYYY-MM-DD&end=YYYY-MM-DD[&tz=Europe/Prague]&units=scientific */
    public Mono<HourlyPointResponse> getHourlyPoint(double lat, double lon, LocalDate date, @Nullable String tz) {
        return client.get()
                .uri(b -> URI.create(buildHourlyPointUri(b, lat, lon, date, tz)))
                .retrieve()
                .bodyToMono(HourlyPointResponse.class);
    }

    private String buildHourlyPointUri(UriBuilder b, double lat, double lon, LocalDate date, @Nullable String tz) {
        var ub = b.path("/point/hourly")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("start", date)
                .queryParam("end", date)
                .queryParam("units", UNITS);
        if (tz != null && !tz.isBlank()) ub.queryParam("tz", tz);
        return ub.build().toString();
    }

    /** /point/daily response */
    public record DailyPointResponse(List<Day> data) {}
    public record Day(
            String date,   // "YYYY-MM-DD"
            Double tmin,   // °C
            Double tmax,   // °C
            Double prcp,   // mm
            Double snow,   // mm
            Double wdir,   // °
            Double wspd,   // m/s (scientific)
            Double wpgt,   // m/s (scientific)
            Double pres,   // hPa
            Double tsun    // min
    ) {}

    /** /point/hourly response */
    public record HourlyPointResponse(List<Hour> data) {}
    public record Hour(
            String time,   // "YYYY-MM-DD HH:mm:ss"
            Double prcp,   // mm
            Integer tsun,  // min sunlight in hour (can be null)
            Integer coco,  // condition code (1–27)
            Double t,      // °C
            Double wdir,   // °
            Double wspd,   // m/s
            Double pres    // hPa
    ) {}
}
