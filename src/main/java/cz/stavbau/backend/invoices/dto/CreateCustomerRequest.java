// src/main/java/cz/stavbau/backend/invoices/dto/CreateCustomerRequest.java
package cz.stavbau.backend.invoices.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateCustomerRequest(
        @NotBlank String type,          // "ORGANIZATION" | "PERSON"
        @NotBlank String name,
        @Size(max = 32) String ico,
        @Size(max = 32) String dic,
        @Email String email,
        @Size(max = 64) String phone,
        @Valid AddressDto billingAddress,
        @PositiveOrZero Integer defaultPaymentTermsDays,
        String notes
) {}
