package cz.stavbau.backend.invoices.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "invoices")
public class Invoice {
    // getters/setters
    @Id
    private UUID id;

    @Column(name="company_id", nullable=false)
    private UUID companyId;

    @Column(name="project_id")
    private UUID projectId;

    @Column(name="number", unique=false) // uniqueness enforced per (company_id, number) index
    private String number;

    @Column(name="issue_date", nullable=false)
    private LocalDate issueDate;

    @Column(name="due_date", nullable=false)
    private LocalDate dueDate;

    @Column(name="tax_date")
    private LocalDate taxDate;

    @Column(length=3, name="currency", nullable=false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name="vat_mode", nullable=false)
    private VatMode vatMode;

    @Column(name="supplier_json", columnDefinition="jsonb", nullable=false)
    private String supplierJson;

    @Column(name="customer_json", columnDefinition="jsonb", nullable=false)
    private String customerJson;

    @Column(name="subtotal", nullable=false, precision=18, scale=2)
    private BigDecimal subtotal;

    @Column(name="vat_total", nullable=false, precision=18, scale=2)
    private BigDecimal vatTotal;

    @Column(name="total", nullable=false, precision=18, scale=2)
    private BigDecimal total;

    @Lob
    @Column(name="notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=16)
    private InvoiceStatus status;

}
