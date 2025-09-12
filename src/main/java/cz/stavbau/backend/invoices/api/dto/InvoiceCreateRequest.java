package cz.stavbau.backend.invoices.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Vytvoření DRAFT faktury")
public record InvoiceCreateRequest(
        @NotNull UUID companyId,
        UUID projectId,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate,
        LocalDate taxDate,
        @NotBlank String currency,
        @NotBlank String supplierJson,
        @NotBlank String customerJson
) {}
