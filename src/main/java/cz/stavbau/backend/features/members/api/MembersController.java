package cz.stavbau.backend.features.members.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.features.members.dto.filters.MemberFilter;
import cz.stavbau.backend.features.members.dto.read.MemberDto;
import cz.stavbau.backend.features.members.dto.read.MemberSummaryDto;
import cz.stavbau.backend.features.members.dto.read.MembersStatsDto;
import cz.stavbau.backend.features.members.dto.write.MemberCreateRequest;
import cz.stavbau.backend.features.members.dto.write.MemberUpdateRequest;
import cz.stavbau.backend.features.members.dto.write.MemberUpdateRolesRequest;
import cz.stavbau.backend.features.members.service.MembersService;
import cz.stavbau.backend.security.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/{companyId}/members")
@Tag(name = "Members", description = "Správa členů firmy (Company Members)")
public class MembersController {

    private final MembersService membersService;

    @GetMapping
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_READ, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)"
    )
    @Operation(summary = "Seznam členů (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<MemberSummaryDto>> findPage(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute MemberFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal,
            HttpServletRequest req
    ) {
        if (log.isDebugEnabled()) {
            log.debug("[Members] QS={} filter={}", req.getQueryString(), filter);
        }
        Page<MemberSummaryDto> page = membersService.findPage(companyId, filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    /** Lookup pro AsyncSearchSelect (vrací value/label, stránkovaně) */
    @GetMapping("/lookup")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_READ, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)"
    )
    @Operation(summary = "Lookup členů (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponse<SelectOptionDto>> lookup(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute MemberFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        Page<SelectOptionDto> page = membersService.lookup(companyId, filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_READ, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)"
    )
    @Operation(summary = "Detail člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MemberDto> get(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(membersService.get(companyId, id));
    }

    @PostMapping
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_WRITE)"
    )
    @Operation(summary = "Vytvořit člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MemberDto> create(
            @PathVariable UUID companyId,
            @RequestBody MemberCreateRequest body,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(membersService.create(companyId, body));
    }

    @PatchMapping("/{id}/profile")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_UPDATE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_UPDATE)"
    )
    @Operation(summary = "Upravit profil člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> updateProfile(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @RequestBody MemberUpdateRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        membersService.updateProfile(companyId, id, req);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_UPDATE_ROLES, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_UPDATE_ROLE)"
    )
    @Operation(summary = "Upravit roli člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> updateRole(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @RequestBody MemberUpdateRolesRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        membersService.updateRole(companyId, id, req);
        return ResponseEntity.noContent().build();
    }

    // --- Archive (soft delete) ---
    @PostMapping("/{id}/archive")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_ARCHIVE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_ARCHIVE)"
    )
    @Operation(summary = "Archivovat člena (soft delete)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> archiveMember(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        membersService.archiveMember(companyId, id);
        return ResponseEntity.noContent().build();
    }

    // --- Unarchive ---
    @PostMapping("/{id}/unarchive")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_ARCHIVE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_ARCHIVE)"
    )
    @Operation(summary = "Obnovit člena (unarchive)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> unarchiveMember(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        membersService.unarchiveMember(companyId, id);
        return ResponseEntity.noContent().build();
    }

    // --- Delete (hard) – povoleno jen pokud člen není použit → service vyhodí 409 (member.in_use), jinak záznam smaže
    @DeleteMapping("/{id}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_REMOVE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_REMOVE)"
    )
    @Operation(summary = "Smazat člena (hard delete if no references)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        membersService.removeMember(companyId, id);
        return ResponseEntity.noContent().build();
    }

    // --- Stats (lightweight přehled) ---
    @GetMapping("/stats")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(#companyId, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).MEMBERS_READ, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)"
    )
    @Operation(summary = "Statistiky členů firmy", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MembersStatsDto> stats(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(membersService.stats(companyId));
    }
}
