package cz.stavbau.backend.features.invoices.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "number_series",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_number_series_company_year_key",
                        columnNames = {"company_id","counter_year","key"})
        })
public class NumberSeries {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "key", nullable = false, length = 32)
    private String key;

    @Column(name = "pattern", nullable = false, length = 64)
    private String pattern; // e.g. "INV-{YYYY}-{NNNN}"

    @Column(name = "counter_year", nullable = false)
    private int counterYear;

    @Column(name = "counter_value", nullable = false)
    private int counterValue;

    @Column(name = "is_default", nullable = false)
    private boolean defaultSeries;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    // --- getters/setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public int getCounterYear() { return counterYear; }
    public void setCounterYear(int counterYear) { this.counterYear = counterYear; }

    public int getCounterValue() { return counterValue; }
    public void setCounterValue(int counterValue) { this.counterValue = counterValue; }

    public boolean isDefaultSeries() { return defaultSeries; }
    public void setDefaultSeries(boolean defaultSeries) { this.defaultSeries = defaultSeries; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
