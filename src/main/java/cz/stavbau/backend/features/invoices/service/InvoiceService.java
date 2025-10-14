package cz.stavbau.backend.features.invoices.service;

import cz.stavbau.backend.features.invoices.model.Invoice;
import cz.stavbau.backend.features.invoices.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceService {

    record LineCreate(String itemName, BigDecimal quantity, String unit, BigDecimal unitPrice, BigDecimal vatRate) {}

    UUID createDraft(UUID companyId, UUID projectId, LocalDate issueDate, LocalDate dueDate, LocalDate taxDate,
                     String currency, String supplierJson, String customerJson);

    void addOrReplaceLines(UUID invoiceId, List<LineCreate> lines);

    void recalcTotals(UUID invoiceId);

    String issue(UUID invoiceId);

    void markPaid(UUID invoiceId);

    void cancel(UUID invoiceId);

    InvoiceStatus getStatus(UUID invoiceId);

    Invoice get(UUID invoiceId);

    Page<Invoice> search(UUID companyId, Optional<UUID> projectId, Optional<InvoiceStatus> status,
                         Optional<String> q, Optional<LocalDate> dateFrom, Optional<LocalDate> dateTo,
                         Pageable pageable);

    void changeStatus(UUID invoiceId, InvoiceStatus status);
}
