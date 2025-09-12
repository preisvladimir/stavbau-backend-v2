// FILE: src/main/java/cz/stavbau/backend/integrations/weather/WeatherSummaryDto.java
package cz.stavbau.backend.integrations.weather;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record WeatherSummaryDto(
        LocalDate date,
        String tz,                 // např. "Europe/Prague"
        String label,              // shrnutí: "Jasno", "Polojasno", ...
        Daily daily,               // číselné denní agregace
        Hourly hourly,             // agregace z hodinových COCO
        String source              // např. "meteostat"
) {
    @Builder
    public record Daily(
            Double tminC,
            Double tmaxC,
            Double windMs,
            Double windGustMs,
            Double prcpMm,
            Double snowMm,
            Double presHpa,
            Integer sunMinutes
    ) {}

    @Builder
    public record Hourly(
            Integer majorConditionCode,     // nejčastější COCO přes den
            Map<Integer, Long> histogram,   // COCO → počet výskytů
            Boolean thunder,                // výskyt bouřek (COCO 25–27)
            Boolean rain,                   // výskyt deště
            Boolean snow                    // výskyt sněžení
    ) {}
}
