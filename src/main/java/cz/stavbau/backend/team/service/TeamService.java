package cz.stavbau.backend.team.service;

import cz.stavbau.backend.team.api.dto.*;

import java.util.UUID;

public interface TeamService {
    MemberDto addMember(UUID companyId, CreateMemberRequest req);
    MemberListResponse listMembers(UUID companyId);
    MemberDto updateRole(UUID companyId, UUID memberId, UpdateMemberRequest req);
    void removeMember(UUID companyId, UUID memberId);
}
