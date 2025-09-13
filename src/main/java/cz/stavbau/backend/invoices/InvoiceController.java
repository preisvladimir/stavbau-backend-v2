package cz.stavbau.backend.invoices;

import cz.stavbau.backend.invoices.api.dto.*;
import cz.stavbau.backend.invoices.model.InvoiceStatus;
import cz.stavbau.backend.invoices.service.InvoiceService;
import cz.stavbau.backend.invoices.util.InvoiceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @PreAuthorize("hasAuthority('invoices:write')")
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
    @PreAuthorize("hasAuthority('invoices:write')")
    @PutMapping(value="/{id}/lines", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> upsertLines(@PathVariable UUID id, @Valid @RequestBody InvoiceLinesUpsertRequest req) {
        List<InvoiceService.LineCreate> lines = req.lines().stream()
                .map(l -> new InvoiceService.LineCreate(l.itemName(), l.quantity(), l.unit(), l.unitPrice(), l.vatRate()))
                .toList();
        invoiceService.addOrReplaceLines(id, lines);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vydá fakturu (ISSUE) a přidělí číslo ze série")
    @PreAuthorize("hasAuthority('invoices:write')")
    @PostMapping("/{id}/issue")
    public ResponseEntity<Map<String, Object>> issue(@PathVariable UUID id) {
        String number = invoiceService.issue(id);
        return ResponseEntity.ok(Map.of("number", number));
    }

    @Operation(summary = "Změna stavu faktury", description = "Povoleno: ISSUED->PAID|CANCELLED")
    @PreAuthorize("hasAuthority('invoices:write')")
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

    @Operation(summary = "Detail faktury")
    @PreAuthorize("hasAuthority('invoices:read')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> detail(@PathVariable UUID id) {
        var inv = invoiceService.get(id);
        return ResponseEntity.ok(InvoiceMapper.toResponse(inv));
    }

    @Operation(
        summary = "Seznam faktur (stránkování, filtry)",
        description = "Filtry: companyId (povinné), projectId, status, q, dateFrom, dateTo; stránkování parametry page, size",
        responses = @ApiResponse(responseCode = "200", description = "PageDto<InvoiceResponse>")
    )
    @PreAuthorize("hasAuthority('invoices:read')")
    @GetMapping
    public ResponseEntity<PageDto<InvoiceResponse>> list(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<cz.stavbau.backend.invoices.model.Invoice> result = invoiceService.search(
                companyId,
                Optional.ofNullable(projectId),
                Optional.ofNullable(status),
                Optional.ofNullable(q),
                Optional.ofNullable(dateFrom),
                Optional.ofNullable(dateTo),
                pageable
        );
        var items = result.getContent().stream().map(InvoiceMapper::toResponse).toList();
        return ResponseEntity.ok(new PageDto<>(items, result.getTotalElements(), result.getNumber(), result.getSize()));
    }
}
