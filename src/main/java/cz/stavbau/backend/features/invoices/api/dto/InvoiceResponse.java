package cz.stavbau.backend.features.invoices.api.dto;

import cz.stavbau.backend.features.invoices.model.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Detail/summary faktury")
public record InvoiceResponse(
        UUID id,
        UUID companyId,
        UUID projectId,
        String number,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate taxDate,
        String currency,
        BigDecimal subtotal,
        BigDecimal vatTotal,
        BigDecimal total,
        InvoiceStatus status
) {}
