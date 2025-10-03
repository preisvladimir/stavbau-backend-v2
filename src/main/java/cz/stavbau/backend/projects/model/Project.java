package cz.stavbau.backend.projects.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private ProjectStatus status;

    @Column(name = "planned_start_date") private LocalDate plannedStartDate;
    @Column(name = "planned_end_date")   private LocalDate plannedEndDate;
    @Column(name = "actual_start_date")  private LocalDate actualStartDate;
    @Column(name = "actual_end_date")    private LocalDate actualEndDate;
    @Column(name = "archived_at")        private Instant archivedAt;

    // JSONB – konfigurováno v JsonbConfig (Hibernate type)
    @Column(name = "site_address_json", columnDefinition = "jsonb")
    private String siteAddressJson;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "vat_mode", length = 16)
    private String vatMode;

    // PostgreSQL text[] (není povinné)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
}
