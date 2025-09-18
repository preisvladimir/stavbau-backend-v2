package cz.stavbau.backend.team.service.impl;

import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {
    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Override
    public MemberDto addMember(UUID companyId, CreateMemberRequest req) {
        // TODO(PR 2/N): implementace, validace, konflikty, audit log
        log.info("TeamService.addMember(companyId={}, email={})", companyId, req.email());
        return null;
    }

    @Override
    public MemberListResponse listMembers(UUID companyId) {
        // TODO(PR 2/N)
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
}
