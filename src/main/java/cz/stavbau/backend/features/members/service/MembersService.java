package cz.stavbau.backend.features.members.service;

import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.features.members.dto.write.MemberCreateRequest;
import cz.stavbau.backend.features.members.dto.read.MemberDto;
import cz.stavbau.backend.features.members.dto.write.MemberUpdateRequest;
import cz.stavbau.backend.features.members.dto.write.MemberUpdateRolesRequest;
import cz.stavbau.backend.features.members.dto.read.MemberSummaryDto;
import cz.stavbau.backend.features.members.dto.read.MembersStatsDto;
import cz.stavbau.backend.features.members.dto.filters.MemberFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MembersService {
    Page<MemberSummaryDto> findPage(UUID companyId,MemberFilter filter, Pageable pageable);
    MemberDto get(UUID companyId, UUID id);
    MemberDto create(UUID companyId, MemberCreateRequest req);
    MemberDto updateProfile(UUID companyId, UUID id, MemberUpdateRequest req);
    MemberDto updateRole(UUID companyId, UUID id, MemberUpdateRolesRequest req);
    void archiveMember(UUID companyId, UUID memberId);
    void unarchiveMember(UUID companyId, UUID memberId);
    void removeMember(UUID companyId, UUID memberId);
    MembersStatsDto stats();
    MembersStatsDto stats(UUID companyId);
    Page<SelectOptionDto> lookup(UUID companyId, MemberFilter filter, Pageable pageable);
}
