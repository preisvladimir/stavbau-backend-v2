package cz.stavbau.backend.features.invoices.api.dto;

import cz.stavbau.backend.features.invoices.model.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Změna stavu faktury")
public record InvoiceStatusChangeRequest(
        @NotNull InvoiceStatus status
) {}
