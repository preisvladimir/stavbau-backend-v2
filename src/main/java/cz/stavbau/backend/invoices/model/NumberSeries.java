package cz.stavbau.backend.invoices.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "number_series")
public class NumberSeries {
    @Id
    private UUID id;
    @Column(nullable=false) private UUID company_id;
    @Column(nullable=false) private String key;
    @Column(nullable=false) private String pattern;
    @Column(nullable=false) private int counter_year;
    @Column(nullable=false) private int counter_value;
    @Column(nullable=false) private boolean is_default;
}
