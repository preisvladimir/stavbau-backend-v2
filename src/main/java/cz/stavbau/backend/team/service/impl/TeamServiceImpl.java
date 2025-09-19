package cz.stavbau.backend.team.service.impl;

import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.team.api.dto.CreateMemberRequest;
import cz.stavbau.backend.team.api.dto.MemberDto;
import cz.stavbau.backend.team.api.dto.MemberListResponse;
import cz.stavbau.backend.team.api.dto.UpdateMemberRequest;
import cz.stavbau.backend.team.mapping.MemberMapper;
import cz.stavbau.backend.team.model.TeamRole;
import cz.stavbau.backend.team.service.TeamService;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.model.UserState;
import cz.stavbau.backend.users.repo.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final Messages messages;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final CompanyMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberDto addMember(UUID companyId, CreateMemberRequest req) {
        final String email = normalizeEmail(req.email());
        validateEmail(email);

        final TeamRole teamRole = requireTeamRole(req.role()); // ADMIN|MEMBER
        final CompanyRoleName companyRole = mapTeamRoleToCompanyRole(teamRole);

        var existingUser = userRepository.findByEmailIgnoreCase(email);
        boolean invited = false;

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (!companyId.equals(user.getCompanyId())) {
                throw new ConflictException(messages.msg("user.assigned_to_other_company"));
            }
            if (memberRepository.existsByCompanyIdAndUserId(companyId, user.getId())) {
                throw new ConflictException(messages.msg("member.exists"));
            }
        } else {
            // Vytvoříme pozvaného uživatele (INVITED) s dočasným hashovaným heslem
            user = new User();
            user.setEmail(email);
            user.setCompanyId(companyId);
            user.setPasswordHash(passwordEncoder.encode(generateRandomSecret()));
            user.setState(UserState.INVITED);
            user.setPasswordNeedsReset(true);
            user.setInvitedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userRepository.save(user);
            invited = true;
        }

        var member = new CompanyMember();
        member.setCompanyId(companyId);
        member.setUserId(user.getId());
        member.setRole(companyRole);
        member.setFirstName(req.firstName());
        member.setLastName(req.lastName());
        member.setPhone(req.phone());
        memberRepository.save(member);

        String status = invited ? "INVITED" : "CREATED";
        return memberMapper.toDto(user, member, status);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberListResponse listMembers(UUID companyId) {
        var members = memberRepository.findByCompanyId(companyId);
        if (members.isEmpty()) {
            return new MemberListResponse(List.of(), 0);
        }

        // in-memory join na uživatele
        Set<UUID> userIds = new HashSet<>();
        for (CompanyMember m : members) userIds.add(m.getUserId());

        Map<UUID, User> users = new HashMap<>();
        for (User u : userRepository.findAllById(userIds)) {
            users.put(u.getId(), u);
        }

        List<MemberDto> items = new ArrayList<>(members.size());
        for (CompanyMember m : members) {
            User u = users.get(m.getUserId());
            if (u != null) {
                items.add(memberMapper.toDto(u, m, "CREATED"));
            }
        }
        return new MemberListResponse(items, items.size());
    }

    @Override
    @Transactional
    public MemberDto updateRole(UUID companyId, UUID memberId, UpdateMemberRequest req) {
        CompanyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        // guard: členství musí patřit do dané firmy
        if (!companyId.equals(member.getCompanyId())) {
            throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
        }

        // nelze měnit OWNER
        if (member.getRole() == CompanyRoleName.OWNER) {
            throw new ForbiddenException(messages.msg("errors.owner.last_owner_forbidden"));
        }

        TeamRole teamRole = requireTeamRole(req.role());
        CompanyRoleName newRole = mapTeamRoleToCompanyRole(teamRole);

        member.setRole(newRole);
        memberRepository.save(member);

        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        return memberMapper.toDto(user, member, "CREATED");
    }

    @Override
    @Transactional
    public void removeMember(UUID companyId, UUID memberId) {
        CompanyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(messages.msg("errors.not.found.member")));

        if (!companyId.equals(member.getCompanyId())) {
            throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
        }

        // zákaz odstranění posledního OWNERa
        if (member.getRole() == CompanyRoleName.OWNER) {
            long owners = memberRepository.countByCompanyIdAndRole(companyId, CompanyRoleName.OWNER);
            if (owners <= 1) {
                throw new ForbiddenException(messages.msg("errors.owner.last_owner_forbidden"));
            }
        }

        memberRepository.delete(member);
    }

    // --- helpery (lokální, bez globálních util) ---

    private CompanyRoleName mapTeamRoleToCompanyRole(TeamRole role) {
        return switch (role) {
            case ADMIN -> CompanyRoleName.COMPANY_ADMIN;
            case MEMBER -> CompanyRoleName.VIEWER; // MEMBER → VIEWER (RBAC 2.1)
        };
    }

    private String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String email) {
        // DTO má @Email, ale po normalizaci ještě hlídáme null/empty (obrana v hloubce)
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException(messages.msg("errors.validation.email"));
        }
    }

    private TeamRole requireTeamRole(String raw) {
        try {
            return TeamRole.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ValidationException(messages.msg("errors.validation.role.invalid"));
        }
    }

    private String generateRandomSecret() {
        byte[] buf = new byte[24];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
