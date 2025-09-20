package cz.stavbau.backend.tenants.membership.repo;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    boolean existsByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);
    List<CompanyMember> findByCompanyId(UUID companyId);
    long countByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    Optional<CompanyMember> findByUserIdAndCompanyId(UUID userId, UUID companyId);
}
