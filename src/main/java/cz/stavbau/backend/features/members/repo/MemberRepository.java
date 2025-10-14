// src/main/java/cz/stavbau/backend/features/members/repo/MemberRepository.java
package cz.stavbau.backend.features.members.repo;

import cz.stavbau.backend.common.simple.IdNameView;
import cz.stavbau.backend.features.members.model.Member;
import cz.stavbau.backend.features.members.repo.projection.MembersStatsProjection;
import cz.stavbau.backend.features.members.repo.projection.RoleCountProjection;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository
        extends JpaRepository<Member, UUID>, JpaSpecificationExecutor<Member> {

    boolean existsByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);
    List<Member> findByCompanyId(UUID companyId);
    long countByCompanyIdAndRole(UUID companyId, CompanyRoleName role);
    Optional<Member> findByUserIdAndCompanyId(UUID userId, UUID companyId);
    Optional<Member> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("""
        select 
          sum(case when m.role = cz.stavbau.backend.security.rbac.CompanyRoleName.OWNER then 1 else 0 end) as owners,
          sum(case when u.state = cz.stavbau.backend.identity.users.model.UserState.ACTIVE then 1 else 0 end)   as active,
          sum(case when u.state = cz.stavbau.backend.identity.users.model.UserState.INVITED then 1 else 0 end)  as invited,
          sum(case when u.state = cz.stavbau.backend.identity.users.model.UserState.DISABLED then 1 else 0 end) as disabled,
          sum(case when m.archivedAt is not null then 1 else 0 end)                                            as archived,
          count(m)                                                                                             as total
        from Member m
        left join m.user u
        where m.companyId = :companyId
        """)
    MembersStatsProjection aggregateMembersStats(@Param("companyId") UUID companyId);

    @Query("""
        select m.role as role, count(m) as cnt
        from Member m
        where m.companyId = :companyId
        group by m.role
        """)
    List<RoleCountProjection> countByRoleGrouped(@Param("companyId") UUID companyId);

    @Query("""
        select m.id as id,
               coalesce(nullif(trim(concat(coalesce(m.firstName,''),' ',coalesce(m.lastName,''))), ''), u.email) as name
        from Member m
        left join m.user u
        where m.companyId = :companyId and m.id in :ids
        """)
    List<IdNameView> findMemberNamesByCompanyAndIdIn(@Param("companyId") UUID companyId,
                                                     @Param("ids") Collection<UUID> ids);
}
