// src/main/java/cz/stavbau/backend/invoices/dto/CreateCustomerRequest.java
package cz.stavbau.backend.invoices.dto;

import jakarta.validation.constraints.*;

public record CreateCustomerRequest(
        @NotBlank String type,          // "ORGANIZATION" | "PERSON"
        @NotBlank String name,
        @Size(max = 32) String ico,
        @Size(max = 32) String dic,
        @Email String email,
        @Size(max = 64) String phone,
        String billingAddressJson,
        @PositiveOrZero Integer defaultPaymentTermsDays,
        String notes
) {}
