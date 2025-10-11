package cz.stavbau.backend.team.service;

import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.dto.TeamSummaryDto;
import cz.stavbau.backend.team.dto.TeamStatsDto;
import cz.stavbau.backend.team.filter.TeamMemberFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {
    Page<TeamSummaryDto> list(TeamMemberFilter filter, Pageable pageable);
    MemberDto get(UUID id);
    MemberDto create(CreateMemberRequest req);
    MemberDto updateProfile(UUID id, UpdateMemberProfileRequest req);
    MemberDto updateRole(UUID id, UpdateMemberRoleRequest req);
    void archiveMember(UUID memberId);
    void unarchiveMember(UUID memberId);
    void removeMember(UUID memberId);
    TeamStatsDto stats();
    TeamStatsDto stats(UUID companyId);
    Page<SelectOptionDto> lookup(TeamMemberFilter filter, Pageable pageable);
}
