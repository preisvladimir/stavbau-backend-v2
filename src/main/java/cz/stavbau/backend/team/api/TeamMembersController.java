package cz.stavbau.backend.team.api;

import cz.stavbau.backend.team.api.dto.*;
import cz.stavbau.backend.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/tenants/{companyId}/members")
@Tag(name = "Team", description = "Správa členů firmy (Team / Company Members)")
public class TeamMembersController {

    private final TeamService teamService;

    public TeamMembersController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(summary = "Přidat člena firmy (ADMIN/MEMBER)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member created",
                    content = @Content(schema = @Schema(implementation = MemberDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden (companyId mismatch / no scope)"),
            @ApiResponse(responseCode = "409", description = "Conflict (member exists / user assigned to other company)")
    })
    @PostMapping
    public ResponseEntity<MemberDto> addMember(
            @PathVariable("companyId") UUID companyId,
            @Valid @RequestBody CreateMemberRequest req
    ) {
        // RBAC + companyId guard přidáme v PR 3/N
        MemberDto created = teamService.addMember(companyId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Seznam členů firmy")
    @GetMapping
    public ResponseEntity<MemberListResponse> listMembers(
            @PathVariable("companyId") UUID companyId
    ) {
        MemberListResponse list = teamService.listMembers(companyId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Změna role člena (ADMIN↔MEMBER)")
    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberDto> updateRole(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody UpdateMemberRequest req
    ) {
        MemberDto updated = teamService.updateRole(companyId, memberId, req);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Odebrat člena")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden (last OWNER)"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId
    ) {
        teamService.removeMember(companyId, memberId);
        return ResponseEntity.noContent().build();
    }
}
