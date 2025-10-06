package cz.stavbau.backend.team.api;

import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.common.persistence.PageableUtils;
import cz.stavbau.backend.common.exception.ForbiddenException;
import cz.stavbau.backend.common.i18n.Messages;
import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.security.rbac.Scopes;
import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.dto.MemberSummaryDto;
import cz.stavbau.backend.team.dto.MembersStatsDto;
import cz.stavbau.backend.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/tenants/{companyId}/members")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Správa členů firmy (Team / Company Members)")
public class TeamMembersController {
    private static final Logger log = LoggerFactory.getLogger(TeamMembersController.class);
    private final TeamService teamService;
    private final Messages messages;
    private final I18nLocaleService i18nLocale;

    private static String nn(String v) { return (v == null || v.isBlank()) ? null : v.trim(); }

    private HttpHeaders i18nHeaders(Locale locale) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        h.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
        return h;
    }

    private void assertCompanyContext(UUID companyId, AppUserPrincipal principal) {
        if (principal == null || principal.getCompanyId() == null || !companyId.equals(principal.getCompanyId())) {
            throw new ForbiddenException(messages.msg("errors.forbidden.company.mismatch"));
        }
    }

    // --- Create ---
    @PostMapping
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_ADD + "')")
    @Operation(summary = "Přidat člena firmy (ADMIN/MEMBER)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member created",
                    content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    public ResponseEntity<MemberDto> addMember(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateMemberRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        var created = teamService.addMember(companyId, req);
        return ResponseEntity
                .created(URI.create("/api/v1/tenants/" + companyId + "/members/" + created.memberId()))
                .headers(i18nHeaders(loc))
                .body(created);
    }

    // --- List (paged) ---
    @GetMapping
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)")
    @Operation(
            summary = "Seznam členů firmy (paged)",
            description = "Fulltext přes email/jméno/telefon parametrem `q` + volitelný filtr `role`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizováno"),
            @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)")
    })
    public ResponseEntity<Page<MemberSummaryDto>> list(
            @PathVariable UUID companyId,
            @Parameter(description = "Fulltext (email, jméno, telefon)") @RequestParam(required = false) String q,
            @Parameter(description = "Filtr role (OWNER/COMPANY_ADMIN/…)")
            @RequestParam(required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "user.email,asc") String sort,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        log.warn("Sort '{}'.", sort);
        Pageable pageable = PageableUtils.from(
                sort, page, size,
                /* default */ "user.email",
                /* allowed */ Set.of(
                        "id", "firstName", "lastName", "phone", "user.role",
                        "createdAt", "updatedAt",
                        "user.email", "user.state"
                ),
                /* aliases */ Map.of(
                        "email", "user.email",
                        "state", "user.state",
                        "name", "lastName",
                        "role", "user.role"

                )
        );
        log.warn( pageable.toString() );
        String qNorm = (q == null || q.isBlank()) ? null : q.trim();
        String roleNorm = (role == null || role.isBlank()) ? null : role.trim().toUpperCase(Locale.ROOT);

        var data = teamService.list(qNorm, roleNorm, pageable);
        log.warn( data.toString() );
        return new ResponseEntity<>(data, i18nHeaders(loc), HttpStatus.OK);
    }

    // --- Detail (profile) ---
    @GetMapping("/{memberId}/profile")
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).TEAM_READ)")
    @Operation(summary = "Detail člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MemberDto> detail(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        var dto = teamService.getMember(companyId, memberId);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    // --- Update profile ---
    @PatchMapping("/{memberId}/profile")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_UPDATE + "')")
    @Operation(summary = "Aktualizace profilových údajů člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MemberDto> updateProfile(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberProfileRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        var dto = teamService.updateProfile(companyId, memberId, req);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    // --- Update role ---
    @PatchMapping("/{memberId}")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_UPDATE_ROLE + "')")
    @Operation(summary = "Změna role člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MemberDto> updateRole(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        var dto = teamService.updateRole(companyId, memberId, req);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    // --- Delete ---
    @DeleteMapping("/{memberId}")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_REMOVE + "')")
    @Operation(summary = "Odebrat člena", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        teamService.removeMember(companyId, memberId);
        return ResponseEntity.noContent().headers(i18nHeaders(loc)).build();
    }

    // --- Stats ---
    @GetMapping("/stats")
    @PreAuthorize("@rbac.hasAnyScope('" + Scopes.TEAM_WRITE + "','" + Scopes.TEAM_READ + "')")
    @Operation(summary = "Statistiky členů firmy", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MembersStatsDto> getMembersStats(@PathVariable UUID companyId) {
        var loc = i18nLocale.resolve();
        var body = teamService.getMembersStats(companyId);
        return new ResponseEntity<>(body, i18nHeaders(loc), HttpStatus.OK);
    }

    // --- (volitelné) Deprecated list ---
    @Deprecated
    @GetMapping("/list")
    @PreAuthorize("@rbac.hasScope('" + Scopes.TEAM_READ + "')")
    public ResponseEntity<MemberListResponse> listMembers(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        assertCompanyContext(companyId, principal);
        var loc = i18nLocale.resolve();
        var list = teamService.listMembers(companyId);
        return new ResponseEntity<>(list, i18nHeaders(loc), HttpStatus.OK);
    }
}
