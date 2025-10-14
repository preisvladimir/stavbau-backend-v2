package cz.stavbau.backend.features.members.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import cz.stavbau.backend.identity.users.model.User;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = "user")
@Entity(name = "Member") // JPQL jméno entity (repo už používá "Member m")
@Table(
        name = "company_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_company_members_company_user", columnNames = {"company_id", "user_id"})
        },
        indexes = {
                @Index(name = "ix_members_company", columnList = "company_id"),
                @Index(name = "ix_members_company_role", columnList = "company_id,role"),
                @Index(name = "ix_members_archived_at", columnList = "archived_at"),
                @Index(name = "ix_members_user", columnList = "user_id")
        }
)
public class Member extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "company_id", nullable = false)
    @Comment("Tenant guard – všechny dotazy scope-ované firmou")
    private UUID companyId;

    @Column(name = "user_id", nullable = false)
    @Comment("FK na identity.users (držíme jako UUID; vazba níže je read-only)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private CompanyRoleName role;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone", length = 40)
    private String phone;

    /** Read-only reference na uživatele (JOIN pro čtení/specifikace) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @Column(name = "archived_at")
    private Instant archivedAt;

    public boolean isArchived() { return archivedAt != null; }

    /** Idempotentní přepínač archivace (čas nastavujeme zde) */
    public void setArchived(boolean archived) {
        this.archivedAt = archived ? Instant.now() : null;
    }
}
