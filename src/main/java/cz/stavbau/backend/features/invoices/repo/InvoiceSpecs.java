package cz.stavbau.backend.features.invoices.repo;

import cz.stavbau.backend.features.invoices.model.Invoice;
import cz.stavbau.backend.features.invoices.model.InvoiceStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class InvoiceSpecs {
    private InvoiceSpecs(){}

    public static Specification<Invoice> company(UUID companyId) {
        return (root, q, cb) -> cb.equal(root.get("companyId"), companyId);
    }
    public static Specification<Invoice> project(UUID projectId) {
        return (root, q, cb) -> cb.equal(root.get("projectId"), projectId);
    }
    public static Specification<Invoice> status(InvoiceStatus status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }
    public static Specification<Invoice> dateFrom(LocalDate from) {
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("issueDate"), from);
    }
    public static Specification<Invoice> dateTo(LocalDate to) {
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("issueDate"), to);
    }
    public static Specification<Invoice> q(String text) {
        String like = "%" + text.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
            cb.like(cb.lower(root.get("number")), like),
            cb.like(cb.lower(root.get("supplierJson")), like),
            cb.like(cb.lower(root.get("customerJson")), like)
        );
    }
}
