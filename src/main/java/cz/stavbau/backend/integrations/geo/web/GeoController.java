package cz.stavbau.backend.integrations.geo.web;

import cz.stavbau.backend.integrations.geo.dto.AddressSuggestion;
import cz.stavbau.backend.integrations.geo.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/geo")
@Tag(name = "Geo", description = "Geocoding & suggestions (Mapy.cz)")
@Validated
public class GeoController {

    private final GeoService geoService;

    public GeoController(GeoService geoService) {
        this.geoService = geoService;
    }

    @GetMapping("/suggest")
    @Operation(summary = "Address suggestions", description = "Autocomplete adres dle dotazu (server-side kvóta/caching).")
    public Mono<List<AddressSuggestion>> suggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "7") Integer limit,
            @RequestParam(required = false) String lang
    ) {
        return geoService.suggest(q, limit, lang);
    }
}
