package cz.stavbau.backend.invoices.service;

import cz.stavbau.backend.invoices.model.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    record LineCreate(String itemName, BigDecimal quantity, String unit, BigDecimal unitPrice, BigDecimal vatRate) {}

    UUID createDraft(UUID companyId, UUID projectId, LocalDate issueDate, LocalDate dueDate, LocalDate taxDate,
                     String currency, String supplierJson, String customerJson);

    void addOrReplaceLines(UUID invoiceId, List<LineCreate> lines);

    void recalcTotals(UUID invoiceId);

    String issue(UUID invoiceId); // returns assigned number

    void markPaid(UUID invoiceId);

    void cancel(UUID invoiceId);

    InvoiceStatus getStatus(UUID invoiceId);
}
