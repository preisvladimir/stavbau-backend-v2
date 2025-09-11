package cz.stavbau.backend.integrations.geo.web;

import cz.stavbau.backend.integrations.geo.dto.AddressSuggestion;
import cz.stavbau.backend.integrations.geo.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
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
    @Operation(
            summary = "Address suggestions",
            description = "Autocomplete adres dle dotazu. " +
                    "Výsledky jsou cachované (server-side) a volání je rate-limitované."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Seznam návrhů",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressSuggestion.class)))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Neplatné parametry (např. q příliš krátké)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
            responseCode = "429",
            description = "Rate limit překročen",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
            responseCode = "502",
            description = "Chyba externího provideru",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public Mono<List<AddressSuggestion>> suggest(
            @Parameter(name = "q", description = "Hledaný text (min. 2 znaky).", example = "Praha")
            @RequestParam(name = "q") String q,

            @Parameter(name = "limit", description = "Limit počtu položek (1–10).", example = "5")
            @RequestParam(name = "limit", defaultValue = "7") Integer limit,

            @Parameter(name = "lang", description = "Jazyk výsledků (pokud provider podporuje).", example = "cs")
            @RequestParam(name = "lang", required = false) String lang
    ) {
        return geoService.suggest(q, limit, lang);
    }
}
