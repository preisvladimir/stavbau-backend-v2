// src/main/java/cz/stavbau/backend/invoices/dto/CustomerSummaryDto.java
package cz.stavbau.backend.invoices.dto;

import java.util.UUID;

public record CustomerSummaryDto(
        UUID id,
        String name,
        String ico,
        String dic,
        String email
) {}
