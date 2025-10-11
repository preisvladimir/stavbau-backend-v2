package cz.stavbau.backend.projects.model;

import cz.stavbau.backend.common.domain.Address;
import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "projects",
        uniqueConstraints = @UniqueConstraint(name="uq_projects_company_code", columnNames = {"company_id","code"}))
public class Project extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "project_manager_id")
    private UUID projectManagerId;

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 24)
    private ProjectType type;            // NEW_BUILD, RECONSTRUCTION ...

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private ProjectStatus status;

    @Column(name = "planned_start_date") private LocalDate plannedStartDate;
    @Column(name = "planned_end_date")   private LocalDate plannedEndDate;
    @Column(name = "actual_start_date")  private LocalDate actualStartDate;
    @Column(name = "actual_end_date")    private LocalDate actualEndDate;
    @Column(name = "archived_at")        private Instant archivedAt;

    @Convert(converter = cz.stavbau.backend.common.persistence.AddressJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "site_address", columnDefinition = "jsonb")
    private Address siteAddress;

    @Deprecated
    @Column(name = "site_address", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String siteAddressJson;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "vat_mode", length = 16)
    private String vatMode;

    @Column(name = "contract_value_net", precision = 18, scale = 2)
    private BigDecimal contractValueNet;

    @Column(name = "contract_value_gross", precision = 18, scale = 2)
    private BigDecimal contractValueGross;

    @Column(name = "retention_percent", precision = 5, scale = 2)
    private BigDecimal retentionPercent;

    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays;

    @Column(name = "external_ref", length = 64)
    private String externalRef;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
}