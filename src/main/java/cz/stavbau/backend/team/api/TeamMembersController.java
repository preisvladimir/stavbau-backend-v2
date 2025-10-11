package cz.stavbau.backend.team.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.dto.TeamSummaryDto;
import cz.stavbau.backend.team.dto.TeamStatsDto ;
import cz.stavbau.backend.team.filter.TeamMemberFilter;
import cz.stavbau.backend.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/{companyId}/members")
@Tag(name = "Team", description = "Správa členů firmy (Team / Company Members)")
public class TeamMembersController {
    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)")
    @Operation(summary = "Seznam členů (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<TeamSummaryDto>> list(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute TeamMemberFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal,
            HttpServletRequest req
    ) {
        if (log.isDebugEnabled()) {
            log.debug("[Team] QS={} filter={}", req.getQueryString(), filter);
        }
        Page<TeamSummaryDto> page = teamService.list(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    /** Lookup pro AsyncSearchSelect (vrací value/label, stránkovaně) */
    @GetMapping("/lookup")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)")
    @Operation(summary = "Lookup členů (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponse<SelectOptionDto>> lookup(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute TeamMemberFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        Page<SelectOptionDto> page = teamService.lookup(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)")
    @Operation(summary = "Detail člena")
    public ResponseEntity<MemberDto> get(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(teamService.get(id));
    }

    @PostMapping
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_WRITE)")
    @Operation(summary = "Vytvořit člena")
    public ResponseEntity<MemberDto> create(
            @PathVariable UUID companyId,
            @RequestBody CreateMemberRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(teamService.create(req));
    }

    @PatchMapping("/{id}/profile")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_UPDATE)")
    @Operation(summary = "Upravit profil člena")
    public ResponseEntity<Void> updateProfile(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @RequestBody UpdateMemberProfileRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        teamService.updateProfile(id, req);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_UPDATE)")
    @Operation(summary = "Upravit roli člena")
    public ResponseEntity<Void> updateRole(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @RequestBody UpdateMemberRoleRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        teamService.updateRole(id, req);
        return ResponseEntity.noContent().build();
    }

    // --- Archive (soft delete) ---
    @PostMapping("/{id}/archive")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_ARCHIVE)")
    @Operation(summary = "Archive member (soft delete)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> archiveMember(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        teamService.archiveMember(id);
        return ResponseEntity.noContent().build();
    }

    // --- Unarchive ---
    @PostMapping("/{id}/unarchive")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_ARCHIVE)")
    @Operation(summary = "Unarchive member", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> unarchiveMember(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        teamService.unarchiveMember(id);
        return ResponseEntity.noContent().build();
    }

    // --- Delete (hard) – povoleno jen pokud člen není použit →
    // service vyhodí 409 (member.in_use), jinak záznam smaže
    @DeleteMapping("/{id}")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_REMOVE)")
    @Operation(summary = "Delete member (hard delete if no references)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        teamService.removeMember(id);
        return ResponseEntity.noContent().build();
    }

    // --- Stats (lightweight přehled) ---
    @GetMapping("/stats")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)")
    @Operation(summary = "Team statistics for the company", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TeamStatsDto> stats(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(teamService.stats());
    }


}
