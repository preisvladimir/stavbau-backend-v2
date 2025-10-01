package cz.stavbau.backend.tenants.membership.repo;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.team.repo.projection.MembersStatsTuple;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID>, JpaSpecificationExecutor<CompanyMember> {
    boolean existsByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);
    List<CompanyMember> findByCompanyId(UUID companyId);
    long countByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    Optional<CompanyMember> findByUserIdAndCompanyId(UUID userId, UUID companyId);

    @Query("""
  select 
    sum(case when m.role = cz.stavbau.backend.security.rbac.CompanyRoleName.OWNER then 1 else 0 end) as owners,
    sum(case when u.state = cz.stavbau.backend.users.model.UserState.ACTIVE then 1 else 0 end) as active,
    sum(case when u.state = cz.stavbau.backend.users.model.UserState.INVITED then 1 else 0 end) as invited,
    sum(case when u.state = cz.stavbau.backend.users.model.UserState.DISABLED then 1 else 0 end) as disabled,
    count(m) as total
  from CompanyMember m
  join m.user u
  where m.companyId = :companyId
""")
    MembersStatsTuple aggregateMembersStats(@Param("companyId") UUID companyId);
}
