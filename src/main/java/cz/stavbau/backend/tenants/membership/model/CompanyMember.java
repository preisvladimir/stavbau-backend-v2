package cz.stavbau.backend.tenants.membership.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
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
}
