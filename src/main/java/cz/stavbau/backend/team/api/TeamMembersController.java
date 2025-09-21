package cz.stavbau.backend.team.api;

import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.security.AppUserPrincipal; // <- dle tvého projektu
import cz.stavbau.backend.security.rbac.Scopes;
import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{companyId}/members")
@Tag(name = "Team", description = "Správa členů firmy (Team / Company Members)")
@RequiredArgsConstructor
public class TeamMembersController {

    private final TeamService teamService;
    private final Messages messages;

    // -- helpers --

    private void assertCompanyContext(UUID companyId, AppUserPrincipal principal) {
        if (principal == null || principal.getCompanyId() == null || !companyId.equals(principal.getCompanyId())) {
            throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
        }
    }

    // Pokud chceš být 100% tolerantní k typu, můžeš nechat i tuto variantu:
     private void assertCompanyContext(UUID companyId, Object principal) {
        if (principal instanceof AppUserPrincipal p && p.getCompanyId() != null && companyId.equals(p.getCompanyId())) {
             return;
         }
         throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
    }

    // -- endpoints --

    @Operation(summary = "Přidat člena firmy (ADMIN/MEMBER)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member created",
                         content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden (companyId mismatch / missing scope)"),
            @ApiResponse(responseCode = "409", description = "Conflict (member exists / user assigned to other company)")
    })
    @PostMapping
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_ADD + "')")
    public ResponseEntity<MemberDto> addMember(
            @PathVariable("companyId") UUID companyId,
            @Valid @RequestBody CreateMemberRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var created = teamService.addMember(companyId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Seznam členů firmy")
    @GetMapping
    @PreAuthorize("@rbac.hasScope('" + Scopes.TEAM_READ + "')")
    public ResponseEntity<MemberListResponse> listMembers(
            @PathVariable("companyId") UUID companyId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var list = teamService.listMembers(companyId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Aktualizace profilových údajů člena (jméno/příjmení/telefon)")
    @PatchMapping("/{memberId}/profile")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_UPDATE + "')")
    public ResponseEntity<MemberDto> updateProfile(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody UpdateMemberProfileRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var updated = teamService.updateProfile(companyId, memberId, req);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Změna role člena (ADMIN↔MEMBER)")
    @PatchMapping("/{memberId}")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_UPDATE_ROLE + "')")
    public ResponseEntity<MemberDto> updateRole(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var updated = teamService.updateRole(companyId, memberId, req);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Odebrat člena")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden (last OWNER / mismatch / missing scope)"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @DeleteMapping("/{memberId}")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_REMOVE + "')")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        teamService.removeMember(companyId, memberId);
        return ResponseEntity.noContent().build();
    }
}
