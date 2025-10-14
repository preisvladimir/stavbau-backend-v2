// src/main/java/cz/stavbau/backend/invoices/dto/CustomerSummaryDto.java
package cz.stavbau.backend.features.customers.dto;

import cz.stavbau.backend.common.domain.Address;

import java.time.Instant;
import java.util.UUID;

public record CustomerSummaryDto(
        UUID id,
        String name,
        String ico,
        String dic,
        String email,
        Address billingAddress,
        Instant updatedAt,
        Instant createdAt
) {}
