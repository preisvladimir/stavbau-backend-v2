package cz.stavbau.backend.features.members.service.impl;

import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.common.jpa.CommonSpecifications;
import cz.stavbau.backend.common.paging.DomainSortPolicies;
import cz.stavbau.backend.common.paging.PagingPolicy;
import cz.stavbau.backend.common.util.CryptoUtils;
import cz.stavbau.backend.common.util.TextUtils;
import cz.stavbau.backend.common.validation.GlobalValidator;
import cz.stavbau.backend.features.members.dto.filters.MemberFilter;
import cz.stavbau.backend.features.members.dto.read.MemberSummaryDto;
import cz.stavbau.backend.features.members.dto.read.MembersStatsDto;
import cz.stavbau.backend.features.members.model.Member;
import cz.stavbau.backend.features.projects.repo.ProjectRepository;
import cz.stavbau.backend.features.members.dto.write.MemberCreateRequest;
import cz.stavbau.backend.features.members.dto.read.MemberDto;
import cz.stavbau.backend.features.members.dto.write.MemberUpdateRequest;
import cz.stavbau.backend.features.members.dto.write.MemberUpdateRolesRequest;
import cz.stavbau.backend.security.SecurityUtils;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.features.members.dto.filters.MemberFilterUtils;
import cz.stavbau.backend.features.members.mapper.MemberMapper;
import cz.stavbau.backend.features.members.repo.projection.MembersStatsProjection;
import cz.stavbau.backend.features.members.repo.spec.MemberSpecifications;
import cz.stavbau.backend.features.members.service.MembersService;
import cz.stavbau.backend.features.members.repo.MemberRepository;
import cz.stavbau.backend.identity.users.model.User;
import cz.stavbau.backend.identity.users.model.UserState;
import cz.stavbau.backend.identity.users.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class MembersServiceImpl implements MembersService {

    private final Messages messages;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalValidator validator;


    @Override
    @Transactional(readOnly = true)
    public Page<MemberSummaryDto> findPage(UUID companyId, MemberFilter filter, Pageable pageable) {
        // 1) Tenancy guard
        //UUID companyId = SecurityUtils.requireCompanyId();

        // 2) Normalize filtrů (trim, empty->null, UPPER…)
        MemberFilter norm = MemberFilterUtils.normalize(filter);

        // 3) Aliasování sortu a bezpečné stránkování + whitelist řazení
        Sort aliased = PagingPolicy.applyAliases(pageable.getSort(), DomainSortPolicies.MEMBERS_SORT_ALIASES);
        Pageable aliasedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), aliased);

        // 3) Bezpečné stránkování + allow-list řazení
        PageRequest paging = PagingPolicy.ensure(
                aliasedPageable,
                DomainSortPolicies.MEMBERS_MAX_PAGE_SIZE,
                DomainSortPolicies.MEMBERS_DEFAULT_SORT,
                DomainSortPolicies.MEMBERS_ALLOWED_SORT
        );

        // 4) Specifikace s JOIN na user (bez ručního dotahování uživatelů)
        Specification<Member> spec = Specification
                .where(MemberSpecifications.byCompany(companyId))
                .and(CommonSpecifications.notDeleted())
                .and(MemberSpecifications.text(norm.getQ()))
                .and(MemberSpecifications.byRole(norm.getRole()))
                .and(MemberSpecifications.byStatus(norm.getStatus()));

        Page<MemberSummaryDto> page = memberRepository
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
    public MemberDto get(UUID companyId, UUID id) {
        Member m = memberRepository.findById(id)
                .filter(cm -> companyId.equals(cm.getCompanyId()))
                .orElseThrow(() -> new NotFoundException("team.member.notFound"));
        User u = m.getUser(); // lazy
        return memberMapper.toDto(u, m, "CREATED");
    }


    @Override
    @Transactional
    public MemberDto create(UUID companyId, MemberCreateRequest req) {
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

        // Vytvoř Member z požadavku
        var member = new Member();
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
    public MemberDto updateProfile(UUID companyId, UUID memberId, MemberUpdateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));
        if (!companyId.equals(member.getCompanyId())) {
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
    public MemberDto updateRole(UUID companyId, UUID memberId, MemberUpdateRolesRequest req) {
        // 2) Najdi člena a ověř, že patří do stejné firmy
        Member member = memberRepository.findById(memberId)
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
    public MembersStatsDto stats() {
        UUID companyId = SecurityUtils.requireCompanyId();
        return stats(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public MembersStatsDto stats(UUID companyId) {
        MembersStatsProjection agg = memberRepository.aggregateMembersStats(companyId);

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

        return MembersStatsDto.builder()
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
    public Page<SelectOptionDto> lookup(UUID companyId,MemberFilter filter, Pageable pageable) {
        // reuse list() pipeline → ale s malou stránkou a fetchem jen nezbytných sloupců
        Page<MemberSummaryDto> page = findPage(companyId, filter, pageable);
        return page.map(m -> {
            String name = ( (m.getFirstName() != null ? m.getFirstName() : "") + " " +
                    (m.getLastName()  != null ? m.getLastName()  : "") ).trim();
            String label = !name.isEmpty() ? name : (m.getEmail() != null ? m.getEmail() : String.valueOf(m.getId()));
            return new SelectOptionDto(m.getId(), label);
        });
    }

    @Override
    @Transactional
    public void archiveMember(UUID companyId, UUID memberId) {

        Member member = memberRepository.findByIdAndCompanyId(memberId, companyId)
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
    public void unarchiveMember(UUID companyId, UUID memberId) {
        Member member = memberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        if (member.isArchived()) {
            member.setArchived(false); // vynuluje archivedAt
            memberRepository.save(member);
        }
    }

    @Override
    @Transactional
    public void removeMember(UUID companyId, UUID memberId) {
        Member member = memberRepository.findByIdAndCompanyId(memberId, companyId)
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
