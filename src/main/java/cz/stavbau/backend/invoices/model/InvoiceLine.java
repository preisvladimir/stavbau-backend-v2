package cz.stavbau.backend.invoices.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "invoice_lines")
public class InvoiceLine {
    @Id
    private UUID id;

    @Column(name="invoice_id", nullable=false)
    private UUID invoiceId;

    @Column(name="item_name", nullable=false, length=256)
    private String itemName;

    @Column(name="quantity", nullable=false, precision=18, scale=3)
    private BigDecimal quantity;

    @Column(name="unit", nullable=false, length=32)
    private String unit;

    @Column(name="unit_price", nullable=false, precision=18, scale=2)
    private BigDecimal unitPrice;

    @Column(name="vat_rate", nullable=false, precision=5, scale=2)
    private BigDecimal vatRate;

    @Column(name="line_total", nullable=false, precision=18, scale=2)
    private BigDecimal lineTotal;

}
