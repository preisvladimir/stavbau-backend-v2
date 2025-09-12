package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.invoices.api.dto.*;
import cz.stavbau.backend.invoices.model.InvoiceStatus;
import cz.stavbau.backend.invoices.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Invoices", description = "Fakturace – CRUD, řádky, stav, export")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Operation(summary = "Vytvoří DRAFT fakturu")
    @ApiResponse(responseCode = "200", description = "ID vytvořené faktury",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody InvoiceCreateRequest req) {
        UUID id = invoiceService.createDraft(
                req.companyId(),
                req.projectId(),
                req.issueDate(),
                req.dueDate(),
                req.taxDate(),
                req.currency(),
                req.supplierJson(),
                req.customerJson()
        );
        return ResponseEntity.ok(Map.of("id", id));
    }

    @Operation(summary = "Nahradí řádky faktury a přepočítá totals")
    @PutMapping(value="/{id}/lines", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> upsertLines(@PathVariable UUID id, @Valid @RequestBody InvoiceLinesUpsertRequest req) {
        // map DTO -> service record
        List<InvoiceService.LineCreate> lines = req.lines().stream()
                .map(l -> new InvoiceService.LineCreate(l.itemName(), l.quantity(), l.unit(), l.unitPrice(), l.vatRate()))
                .toList();
        invoiceService.addOrReplaceLines(id, lines);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vydá fakturu (ISSUE) a přidělí číslo ze série")
    @PostMapping("/{id}/issue")
    public ResponseEntity<Map<String, Object>> issue(@PathVariable UUID id) {
        String number = invoiceService.issue(id);
        return ResponseEntity.ok(Map.of("number", number));
    }

    @Operation(summary = "Změna stavu faktury", description = "Povoleno: ISSUED->PAID|CANCELLED")
    @PostMapping(value="/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changeStatus(@PathVariable UUID id, @Valid @RequestBody InvoiceStatusChangeRequest req) {
        if (req.status() == InvoiceStatus.PAID) {
            invoiceService.markPaid(id);
        } else if (req.status() == InvoiceStatus.CANCELLED) {
            invoiceService.cancel(id);
        } else {
            throw new IllegalArgumentException("Unsupported transition");
        }
        return ResponseEntity.noContent().build();
    }

    // --- Minimal list/detail pro FE skeleton ---
    @Operation(summary = "Detail faktury")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> detail(@PathVariable UUID id) {
        // NOTE: Simplified mapping – v reálu by se četlo z repo a mapovalo přes mapper
        // Zde pro MVP vracíme placeholder s ID a jen status (ostatní null)
        return ResponseEntity.ok(new InvoiceResponse(
                id, null, null, null, null, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, invoiceService.getStatus(id)
        ));
    }

    @Operation(summary = "Seznam (placeholder)")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        // Placeholder pro FE – reálná implementace bude v dalším kroku
        return ResponseEntity.ok(List.of(Map.of("todo", true)));
    }
}
