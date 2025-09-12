// FILE: src/main/java/cz/stavbau/backend/integrations/weather/IntegrationsWeatherController.java
package cz.stavbau.backend.integrations.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/integrations/weather")
@RequiredArgsConstructor
public class IntegrationsWeatherController {

    private final WeatherSummaryService service;

    /**
     * Inline použití např. při zakládání denního záznamu v Deníku:
     * FE/BE si vyžádá sumář počasí pro danou lokaci a datum.
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()") // MVP: pouze přihlášení; později zpřesnit na scope:diary:write
    public Mono<ResponseEntity<WeatherSummaryDto>> summary(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) Integer alt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.buildDailySummaryDto(lat, lon, alt, date)
                .map(opt -> opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()));
    }
}
