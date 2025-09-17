// +++ src/main/java/cz/stavbau/backend/tenants/dto/CompanyLookupPreviewDto.java
package cz.stavbau.backend.tenants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Normalized preview of company data from ARES for FE form prefill.")
public record CompanyLookupPreviewDto(
        @Schema(example = "03647072") String ico,
        @Schema(example = "CZ12345678", nullable = true) String dic,
        @Schema(example = "Dagmar Horová") String name,
        @Schema(example = "101", description = "ARES/ROS legal form code", nullable = true) String legalFormCode,
        AddressDto address
) {
    @Schema(description = "Simple address shape for FE forms.")
    public record AddressDto(
            @Schema(example = "Pincova 2974/19") String street,
            @Schema(example = "Ústí nad Labem") String city,
            @Schema(example = "400 11") String zip,
            @Schema(example = "CZ") String country
    ) {}
}
