package cz.stavbau.backend.features.invoices.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Řádek faktury")
public record InvoiceLineDto(
        @NotBlank String itemName,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotBlank String unit,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @NotNull @DecimalMin("0.00") BigDecimal vatRate
) {}
