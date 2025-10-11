// src/main/java/cz/stavbau/backend/tenants/membership/repo/CompanyMemberRepository.java
package cz.stavbau.backend.tenants.membership.repo;

import cz.stavbau.backend.common.simple.IdNameView;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.team.repo.projection.RoleCountTuple;
import cz.stavbau.backend.team.repo.projection.TeamStatsTuple;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface CompanyMemberRepository
        extends JpaRepository<CompanyMember, UUID>, JpaSpecificationExecutor<CompanyMember> {

    boolean existsByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);
    List<CompanyMember> findByCompanyId(UUID companyId);
    long countByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    Optional<CompanyMember> findByUserIdAndCompanyId(UUID userId, UUID companyId);
    Optional<CompanyMember> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("""
        select 
          sum(case when m.role = cz.stavbau.backend.security.rbac.CompanyRoleName.OWNER then 1 else 0 end) as owners,
          sum(case when u.state = cz.stavbau.backend.users.model.UserState.ACTIVE then 1 else 0 end)      as active,
          sum(case when u.state = cz.stavbau.backend.users.model.UserState.INVITED then 1 else 0 end)     as invited,
          sum(case when u.state = cz.stavbau.backend.users.model.UserState.DISABLED then 1 else 0 end)    as disabled,
          sum(case when m.archivedAt is not null then 1 else 0 end)                                      as archived,
          count(m)                                                                                       as total
        from CompanyMember m
        left join m.user u
        where m.companyId = :companyId
        """)
    TeamStatsTuple aggregateMembersStats(@Param("companyId") UUID companyId);

    @Query("""
        select m.role as role, count(m) as cnt
        from CompanyMember m
        where m.companyId = :companyId
        group by m.role
        """)
    List<RoleCountTuple> countByRoleGrouped(@Param("companyId") UUID companyId);

    @Query("""
        select m.id as id,
               coalesce(trim(concat(coalesce(m.firstName,''),' ',coalesce(m.lastName,''))), u.email) as name
        from CompanyMember m
        left join m.user u
        where m.companyId = :companyId and m.id in :ids
        """)
    List<IdNameView> findMemberNamesByCompanyAndIdIn(@Param("companyId") UUID companyId,
                                                     @Param("ids") Collection<UUID> ids);
}
