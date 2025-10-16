// src/main/java/cz/stavbau/backend/features/members/model/Member.java
package cz.stavbau.backend.features.members.model;

import cz.stavbau.backend.common.domain.BaseArchivableEntity;
import cz.stavbau.backend.identity.users.model.User;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = "user")
@Entity(name = "Member") // JPQL jméno entity
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
public class Member extends BaseArchivableEntity {

    @EqualsAndHashCode.Include
    @Column(name = "company_id", nullable = false)
    @Comment("Tenant guard – všechny dotazy scope-ované firmou")
    private UUID companyId;

    @Column(name = "user_id", nullable = false)
    @Comment("FK na identity.users (držíme jako UUID; vazba níže je read-only)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private CompanyRoleName role;  // legacy single role (roles/scopes JSONB klidně přidáme vedle)

    @Column(name = "roles", columnDefinition = "jsonb", nullable = false)
    private String roles;          // JSONB array of strings (["OWNER", ...])

    @Column(name = "scopes", columnDefinition = "jsonb", nullable = false)
    private String scopes;         // JSONB array of strings (["projects:read", ...])

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
}
