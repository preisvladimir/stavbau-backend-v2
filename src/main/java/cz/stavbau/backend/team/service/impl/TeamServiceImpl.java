package cz.stavbau.backend.team.service.impl;

import cz.stavbau.backend.common.exception.ConflictException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final Messages messages;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final CompanyMemberRepository memberRepository;

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
            user = new User();
            user.setEmail(email);
            user.setCompanyId(companyId);
            user.setState(UserState.INVITED); // MVP – bez e-mailu
            userRepository.save(user);
            invited = true;
        }

        var member = new CompanyMember();
        member.setCompanyId(companyId);
        member.setUserId(user.getId());
        member.setRole(companyRole);
        memberRepository.save(member);

        String status = invited ? "INVITED" : "CREATED";
        return memberMapper.toDto(user, member, status);
    }

    @Override
    public MemberListResponse listMembers(UUID companyId) {
        // TODO(PR 2/N): načíst členy + stránkování/filtry
        log.info("TeamService.listMembers(companyId={})", companyId);
        return new MemberListResponse(java.util.List.of(), 0);
    }

    @Override
    public MemberDto updateRole(UUID companyId, UUID memberId, UpdateMemberRequest req) {
        // TODO(PR 2/N): zákaz změny OWNER, validace, audit
        log.info("TeamService.updateRole(companyId={}, memberId={}, role={})", companyId, memberId, req.role());
        return null;
    }

    @Override
    public void removeMember(UUID companyId, UUID memberId) {
        // TODO(PR 2/N): hlídat posledního OWNERa
        log.info("TeamService.removeMember(companyId={}, memberId={})", companyId, memberId);
    }

    private CompanyRoleName mapTeamRoleToCompanyRole(TeamRole role) {
        return switch (role) {
            case ADMIN -> CompanyRoleName.COMPANY_ADMIN;
            case MEMBER -> CompanyRoleName.MEMBER;
        };
    }

    private String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String email) {
        // DTO má @Email, ale po normalizaci ještě hlídáme null/empty (obrana v hloubce)
        if (email == null || email.isBlank()) {
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
}
