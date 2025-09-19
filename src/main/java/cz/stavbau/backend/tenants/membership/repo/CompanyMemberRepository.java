package cz.stavbau.backend.tenants.membership.repo;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    boolean existsByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);
    List<CompanyMember> findByCompanyId(UUID companyId);
    long countByCompanyIdAndCompanyRole(UUID companyId, CompanyRoleName role);
    long countByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
}
