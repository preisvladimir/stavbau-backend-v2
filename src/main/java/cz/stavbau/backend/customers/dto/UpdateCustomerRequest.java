// src/main/java/cz/stavbau/backend/invoices/dto/UpdateCustomerRequest.java
package cz.stavbau.backend.customers.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UpdateCustomerRequest(
        String type,
        String name,
        @Size(max = 32) String ico,
        @Size(max = 32) String dic,
        @Email String email,
        @Size(max = 64) String phone,
        @Valid AddressDto billingAddress,
        @PositiveOrZero Integer defaultPaymentTermsDays,
        String notes
) {}
