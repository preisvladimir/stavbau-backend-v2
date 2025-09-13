package cz.stavbau.backend.invoices.util;

import cz.stavbau.backend.invoices.api.dto.InvoiceResponse;
import cz.stavbau.backend.invoices.model.Invoice;

public final class InvoiceMapper {
    private InvoiceMapper(){}
    public static InvoiceResponse toResponse(Invoice i) {
        return new InvoiceResponse(
            i.getId(),
            i.getCompanyId(),
            i.getProjectId(),
            i.getNumber(),
            i.getIssueDate(),
            i.getDueDate(),
            i.getTaxDate(),
            i.getCurrency(),
            i.getSubtotal(),
            i.getVatTotal(),
            i.getTotal(),
            i.getStatus()
        );
    }
}
