package cz.stavbau.backend.integrations.ares.api;

import cz.stavbau.backend.integrations.ares.dto.AresPreviewDto;
import cz.stavbau.backend.integrations.ares.exceptions.AresNotFoundException;
import cz.stavbau.backend.features.companies.dto.CompanyDto;
import cz.stavbau.backend.integrations.ares.mapper.AresPreviewMapper;
import cz.stavbau.backend.features.companies.service.CompanyService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/companies/lookup")
@Validated
@RequiredArgsConstructor
@Tag(name = "Companies • Lookup")
public class AresController {

    private final CompanyService companyService;
    private final AresPreviewMapper previewMapper;

    @Operation(
            summary = "Načti firmu z ARES podle IČO (bez uložení)",
            description = "Vrátí předvyplněná data pro registrační formulář. Data se zatím nepersistují."
    )
    @GetMapping("/ares")
    public ResponseEntity<CompanyDto> lookupByIco(
            @Parameter(
                    name = "ico",
                    description = "IČO (8 číslic)",
                    example = "01820991"
            )
            @RequestParam(name = "ico")
            @Pattern(regexp = "\\d{8}", message = "IČO musí mít 8 číslic")
            String ico) {

        CompanyDto dto = companyService.lookupByAres(ico);
        if (dto == null) {
            throw new AresNotFoundException("Firma s IČO " + ico + " nebyla nalezena v ARES.");
        }
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/ares/preview")
    @Operation(
            summary = "ARES preview (normalized for FE)",
            description = "Returns FE-ready preview of company data from ARES for form prefill. RAW `/ares` stays as-is.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = AresPreviewDto.class))),
                    @ApiResponse(responseCode = "404", description = "Not found (code=ares_not_found)"),
                    @ApiResponse(responseCode = "503", description = "ARES unavailable (code=ares_unavailable)"),
                    @ApiResponse(responseCode = "429", description = "Rate limit (code=rate_limit)")
            }
    )
    public ResponseEntity<AresPreviewDto> getPreview(
            @RequestParam @Pattern(regexp = "\\d{8}") String ico
    ) {
        // Použijeme existující service, která vrací CompanyDto (normalizace na BE)
        CompanyDto dto = companyService.lookupByAres(ico);
        if (dto == null) {
            // Máte-li global exception handler pro RFC7807, hoďte jeho NotFound s code=ares_not_found.
            // Jinak pro MVP lze vrátit 404 bez těla a FE to zmapuje na i18n.
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(previewMapper.toPreview(dto));
    }
}
