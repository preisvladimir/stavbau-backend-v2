// src/main/java/cz/stavbau/backend/invoices/dto/CustomerDto.java
package cz.stavbau.backend.invoices.dto;

import java.util.UUID;

public record CustomerDto(
        UUID id,
        UUID companyId,
        String type,
        String name,
        String ico,
        String dic,
        String email,
        String phone,
        String billingAddressJson,
        Integer defaultPaymentTermsDays,
        String notes,
        UUID linkedUserId
) {}
