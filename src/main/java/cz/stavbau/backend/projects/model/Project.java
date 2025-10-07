package cz.stavbau.backend.projects.model;

import cz.stavbau.backend.common.domain.Address;
import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    /**
      * Adresa stavby (JSONB) – typované pole, stejné chování jako u Customer.billingAddress.
      * Persistuje se jako JSONB pomocí konvertoru.
      */
    @Convert(converter = cz.stavbau.backend.common.persistence.AddressJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "site_address", columnDefinition = "jsonb")
    private Address siteAddress;

    /**
     * Dočasné zpětně kompatibilní pole pro stávající části kódu.
     * Namapováno na stejný sloupec, pouze read-only, aby se předešlo duplicitnímu bindování.
     * TODO: odstranit po úpravě mapperů/DTO (PR naváže).
     */
    @Deprecated
    @Column(name = "site_address", columnDefinition = "jsonb", insertable = false, updatable = false)
    private String siteAddressJson;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "vat_mode", length = 16)
    private String vatMode;

    // PostgreSQL text[] (není povinné)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
}
