package cz.stavbau.backend.team.service;

import cz.stavbau.backend.team.api.dto.*;

import java.util.UUID;

public interface TeamService {
    MemberDto addMember(UUID companyId, CreateMemberRequest req);
    MemberListResponse listMembers(UUID companyId);
    MemberDto updateProfile(UUID companyId, UUID memberId, UpdateMemberProfileRequest req);
    MemberDto updateRole(UUID companyId, UUID memberId, UpdateMemberRoleRequest req);
    void removeMember(UUID companyId, UUID memberId);
}
