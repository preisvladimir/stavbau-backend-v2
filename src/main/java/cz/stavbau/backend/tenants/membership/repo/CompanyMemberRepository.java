package cz.stavbau.backend.tenants.membership.repo;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    boolean existsByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID id);
}
