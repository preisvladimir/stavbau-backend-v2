package cz.stavbau.backend.team.service;

import cz.stavbau.backend.invoices.dto.CustomerSummaryDto;
import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.dto.MembersStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {
    MemberDto addMember(UUID companyId, CreateMemberRequest req);
    MemberListResponse listMembers(UUID companyId);
    MemberDto updateProfile(UUID companyId, UUID memberId, UpdateMemberProfileRequest req);
    MemberDto updateRole(UUID companyId, UUID memberId, UpdateMemberRoleRequest req);
    void removeMember(UUID companyId, UUID memberId);
    Page<MemberDto> searchMembers(UUID companyId, String q, Pageable pageable);
    MemberDto getMember(UUID companyId, UUID memberId);
    MembersStatsDto getMembersStats(UUID companyId);
}
