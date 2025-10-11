package cz.stavbau.backend.team.service.impl;

import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.common.paging.DomainSortPolicies;
import cz.stavbau.backend.common.paging.PagingPolicy;
import cz.stavbau.backend.common.util.CryptoUtils;
import cz.stavbau.backend.common.util.TextUtils;
import cz.stavbau.backend.common.validation.GlobalValidator;
import cz.stavbau.backend.projects.repo.ProjectRepository;
import cz.stavbau.backend.security.SecurityUtils;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.dto.TeamSummaryDto;
import cz.stavbau.backend.team.dto.TeamStatsDto;
import cz.stavbau.backend.team.filter.TeamMemberFilter;
import cz.stavbau.backend.team.filter.TeamMemberFilters;
import cz.stavbau.backend.team.mapping.MemberMapper;
import cz.stavbau.backend.team.repo.projection.TeamStatsTuple;
import cz.stavbau.backend.team.repo.spec.TeamMemberSpecification;
import cz.stavbau.backend.team.service.TeamService;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.model.UserState;
import cz.stavbau.backend.users.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {

    private final Messages messages;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final CompanyMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalValidator validator;


    @Override
    @Transactional(readOnly = true)
    public Page<TeamSummaryDto> list(TeamMemberFilter filter, Pageable pageable) {
        // 1) Tenancy guard
        UUID companyId = SecurityUtils.requireCompanyId();

        // 2) Normalize filtrů (trim, empty->null, UPPER…)
        TeamMemberFilter norm = TeamMemberFilters.normalize(filter);

        // 3) Bezpečné stránkování + allow-list řazení
        PageRequest paging = PagingPolicy.ensure(
                pageable,
                DomainSortPolicies.TEAM_MAX_PAGE_SIZE,
                DomainSortPolicies.TEAM_DEFAULT_SORT,
                DomainSortPolicies.TEAM_ALLOWED_SORT
        );

        // 4) Specifikace s JOIN na user (bez ručního dotahování uživatelů)
        Specification<CompanyMember> spec = new TeamMemberSpecification(companyId, norm);

        Page<TeamSummaryDto> page = memberRepository
                .findAll(spec, paging)
                .map(memberMapper::toSummaryDto);

        // 5) UX fallback – prázdná stránka (ale existují předchozí záznamy) → o 1 zpět
        if (page.isEmpty() && page.getTotalElements() > 0 && paging.getPageNumber() > 0) {
            PageRequest prev = PageRequest.of(paging.getPageNumber() - 1, paging.getPageSize(), paging.getSort());
            page = memberRepository.findAll(spec, prev).map(memberMapper::toSummaryDto);
        }

        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto get(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        CompanyMember m = memberRepository.findById(id)
                .filter(cm -> companyId.equals(cm.getCompanyId()))
                .orElseThrow(() -> new NotFoundException("team.member.notFound"));
        User u = m.getUser(); // lazy
        return memberMapper.toDto(u, m, "CREATED");
    }


    @Override
    @Transactional
    public MemberDto create(CreateMemberRequest req) {
        // Tenant guard
        UUID companyId = SecurityUtils.requireCompanyId();

        // Validace vstupů (normalizovaný email + enum role)
        final String email = validator.requireValidEmail(req.email());
        final CompanyRoleName companyRole =
                validator.requireEnum(CompanyRoleName.class, req.role(), "errors.validation.role.invalid");

        // Pokus najít uživatele podle emailu
        var existingUser = userRepository.findByEmailIgnoreCase(email);
        boolean invited = false;

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();

            // Uživatel existuje, ale je přiřazen k jiné firmě -> konflikt
            if (!companyId.equals(user.getCompanyId())) {
                throw new ConflictException(messages.msg("user.assigned_to_other_company"));
            }
            // Už je členem této firmy -> konflikt
            if (memberRepository.existsByCompanyIdAndUserId(companyId, user.getId())) {
                throw new ConflictException(messages.msg("member.exists"));
            }
        } else {
            // Založ „pozvaného“ uživatele
            user = new User();
            user.setEmail(email);
            user.setCompanyId(companyId);
            user.setPasswordHash(passwordEncoder.encode(CryptoUtils.randomUrlSafeSecret()));
            user.setState(UserState.INVITED);
            user.setPasswordNeedsReset(true);
            user.setInvitedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userRepository.save(user);
            invited = true;
        }

        // Vytvoř CompanyMember z požadavku
        var member = new CompanyMember();
        member.setCompanyId(companyId);
        member.setUserId(user.getId());
        member.setRole(companyRole);
        member.setFirstName(req.firstName());
        member.setLastName(req.lastName());
        member.setPhone(req.phone());
        memberRepository.save(member);

        // Stav pro FE (např. badge v detailu)
        String status = invited ? "INVITED" : "CREATED";
        return memberMapper.toDto(user, member, status);
    }

    @Override
    @Transactional
    public MemberDto updateProfile(UUID memberId, UpdateMemberProfileRequest req) {

        UUID tenantId = SecurityUtils.requireCompanyId();
        // Najdi člena podle ID a ověř, že patří do firmy
        CompanyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));
        if (!tenantId.equals(member.getCompanyId())) {
            throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
        }

        boolean changed = false;

        // PATCH sémantika: měníme jen pole, která v requestu přišla (req.xxx() != null)
        if (req.firstName() != null) {
            String v = TextUtils.normalizeBlankToNull(req.firstName());
            if (!Objects.equals(v, member.getFirstName())) { member.setFirstName(v); changed = true; }
        }
        if (req.lastName() != null) {
            String v = TextUtils.normalizeBlankToNull(req.lastName());
            if (!Objects.equals(v, member.getLastName())) { member.setLastName(v); changed = true; }
        }
        if (req.phone() != null) {
            String v = TextUtils.normalizeBlankToNull(req.phone());
            if (!Objects.equals(v, member.getPhone())) { member.setPhone(v); changed = true; }
        }

        if (changed) {
            memberRepository.save(member);
        }

        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.user")));

        return memberMapper.toDto(user, member, changed ? "UPDATED" : "UNCHANGED");
    }

    @Override
    @Transactional
    public MemberDto updateRole(UUID memberId, UpdateMemberRoleRequest req) {
        // 1) Tenant guard (defense-in-depth)
        UUID companyId = SecurityUtils.requireCompanyId();

        // 2) Najdi člena a ověř, že patří do stejné firmy
        CompanyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));
        if (!companyId.equals(member.getCompanyId())) {
            throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
        }

        // 3) Validace cílové role
        CompanyRoleName newRole = validator.requireEnum(
                CompanyRoleName.class,
                req.role(),
                "errors.validation.role.invalid"
        );

        // 4) Pokud se role fakticky nemění → nic neukládej
        CompanyRoleName oldRole = member.getRole();
        if (oldRole == newRole) {
            User u = userRepository.findById(member.getUserId())
                    .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.user")));
            return memberMapper.toDto(u, member, "UNCHANGED");
        }

        // 5) Zákaz odebrat roli OWNER, pokud je to poslední OWNER ve firmě
        if (oldRole == CompanyRoleName.OWNER && newRole != CompanyRoleName.OWNER) {
            long owners = memberRepository.countByCompanyIdAndRole(companyId, CompanyRoleName.OWNER);
            // když je pouze jeden OWNER (tento), nelze ho degradovat
            if (owners <= 1) {
                throw new ForbiddenException(messages.msg("errors.owner.last_owner_forbidden"));
            }
        }

        // 6) Ulož změnu role
        member.setRole(newRole);
        memberRepository.save(member);

        // 7) Sestav výstup
        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.user")));
        return memberMapper.toDto(user, member, "UPDATED");
    }

    @Override
    @Transactional(readOnly = true)
    public TeamStatsDto stats() {
        UUID companyId = SecurityUtils.requireCompanyId();
        return stats(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamStatsDto stats(UUID companyId) {
        TeamStatsTuple agg = memberRepository.aggregateMembersStats(companyId);

        long owners   = TextUtils.nz(agg == null ? null : agg.getOwners());
        long active   = TextUtils.nz(agg == null ? null : agg.getActive());
        long invited  = TextUtils.nz(agg == null ? null : agg.getInvited());
        long disabled = TextUtils.nz(agg == null ? null : agg.getDisabled());
        long archived = TextUtils.nz(agg == null ? null : agg.getArchived());
        long total    = TextUtils.nz(agg == null ? null : agg.getTotal());

        // rozpad podle rolí → převedeme na Map
        var roleRows = memberRepository.countByRoleGrouped(companyId);
        var byRole = new java.util.EnumMap<CompanyRoleName, Long>(CompanyRoleName.class);
        for (var row : roleRows) {
            byRole.put(row.getRole(), TextUtils.nz(row.getCnt()));
        }

        return TeamStatsDto.builder()
                .owners(owners)
                .active(active)
                .invited(invited)
                .disabled(disabled)
                .archived(archived)
                .total(total)
                .byRole(byRole)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SelectOptionDto> lookup(TeamMemberFilter filter, Pageable pageable) {
        // reuse list() pipeline → ale s malou stránkou a fetchem jen nezbytných sloupců
        Page<TeamSummaryDto> page = list(filter, pageable);
        return page.map(m -> {
            String name = ( (m.getFirstName() != null ? m.getFirstName() : "") + " " +
                    (m.getLastName()  != null ? m.getLastName()  : "") ).trim();
            String label = !name.isEmpty() ? name : (m.getEmail() != null ? m.getEmail() : String.valueOf(m.getId()));
            return new SelectOptionDto(m.getId(), label);
        });
    }

    @Override
    @Transactional
    public void archiveMember(UUID memberId) {
        UUID companyId = SecurityUtils.requireCompanyId();

        CompanyMember member = memberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        // Nesmíš archivovat posledního OWNERa (jinak by firma zůstala bez správce)
        if (member.getRole() == CompanyRoleName.OWNER) {
            long owners = memberRepository.countByCompanyIdAndRole(companyId, CompanyRoleName.OWNER);
            if (owners <= 1) {
                throw new ForbiddenException(messages.msg("errors.owner.last_owner_forbidden"));
            }
        }

        if (!member.isArchived()) {
            member.setArchived(true); // nastaví archivedAt = now
            memberRepository.save(member);
        }
    }

    @Override
    @Transactional
    public void unarchiveMember(UUID memberId) {
        UUID companyId = SecurityUtils.requireCompanyId();

        CompanyMember member = memberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        if (member.isArchived()) {
            member.setArchived(false); // vynuluje archivedAt
            memberRepository.save(member);
        }
    }

    @Override
    @Transactional
    public void removeMember(UUID memberId) {
        UUID companyId = SecurityUtils.requireCompanyId();

        CompanyMember member = memberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        // zákaz odstranění posledního OWNERa
        if (member.getRole() == CompanyRoleName.OWNER) {
            long owners = memberRepository.countByCompanyIdAndRole(companyId, CompanyRoleName.OWNER);
            if (owners <= 1) {
                throw new ForbiddenException(messages.msg("errors.owner.last_owner_forbidden"));
            }
        }

        // ---- referenční kontroly (přizpůsob svému modelu) ----
        boolean usedAsPm = projectRepository.existsByCompanyIdAndProjectManagerId(
                companyId,
                /* pokud PM je userId:member.getUserId() */
                /* pokud PM je memberId: */ member.getId()
        );

        // příklady dalších kontrol:
      //  boolean usedInProjects = projectRepository
               // .existsByCompanyIdAndParticipants_MemberId(companyId, member.getId());
        // boolean usedInTimesheets = timesheetRepository.existsByCompanyIdAndUserId(companyId, member.getUserId());
        // boolean usedInComments  = commentRepository.existsByCompanyIdAndAuthorId(companyId, member.getUserId());

        if (usedAsPm /* ||  usedInProjects|| usedInTimesheets || usedInComments */) {
            // legislativně i prakticky: nechat v systému -> archivovat místo hard delete
            throw new ConflictException(messages.msg("member.in_use")); // 409 s i18n klíčem
        }

        // Bez vazeb → hard delete je v pořádku
        memberRepository.delete(member);
    }

}
