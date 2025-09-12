package cz.stavbau.backend.invoices.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "invoice_lines")
public class InvoiceLine {
    @Id
    private UUID id;
    @Column(nullable=false) private UUID invoice_id;
    private String item_name;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unit_price;
    private BigDecimal vat_rate;
    private BigDecimal line_total;
}
