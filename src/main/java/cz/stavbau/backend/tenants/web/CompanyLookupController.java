package cz.stavbau.backend.tenants.web;

import cz.stavbau.backend.integrations.ares.exceptions.AresNotFoundException;
import cz.stavbau.backend.tenants.dto.CompanyDto;
import cz.stavbau.backend.tenants.service.CompanyService;
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
public class CompanyLookupController {

    private final CompanyService companyService;

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

}
