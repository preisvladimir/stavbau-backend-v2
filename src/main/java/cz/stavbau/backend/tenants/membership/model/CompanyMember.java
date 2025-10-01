package cz.stavbau.backend.tenants.membership.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.users.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "company_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_company_members_company_user", columnNames = {"company_id", "user_id"})
        }
)
public class CompanyMember extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "user_id", nullable = false)
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

    /** Read-only reference na uživatele (kvůli dotazům a mapování). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;  // nepoužíváme pro zápis, jen pro JOIN ve Specification

}
