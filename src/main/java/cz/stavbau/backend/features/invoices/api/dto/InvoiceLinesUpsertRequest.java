package cz.stavbau.backend.features.invoices.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Nahrazení (upsert) řádků faktury")
public record InvoiceLinesUpsertRequest(
        @Valid @NotEmpty List<InvoiceLineDto> lines
) {}
