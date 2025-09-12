package cz.stavbau.backend.invoices.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    private UUID id;
    @Column(nullable=false) private UUID company_id;
    private UUID project_id;
    private String number;
    private LocalDate issue_date;
    private LocalDate due_date;
    private LocalDate tax_date;
    @Column(length=3) private String currency;
    private String vat_mode;
    @Column(columnDefinition="jsonb") private String supplier_json;
    @Column(columnDefinition="jsonb") private String customer_json;
    private BigDecimal subtotal;
    private BigDecimal vat_total;
    private BigDecimal total;
    @Lob private String notes;
    private String status;

}
