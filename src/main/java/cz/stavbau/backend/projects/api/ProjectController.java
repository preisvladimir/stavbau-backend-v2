package cz.stavbau.backend.projects.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.filter.ProjectFilter;
import cz.stavbau.backend.projects.service.ProjectService;
import cz.stavbau.backend.security.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/{companyId}/projects")
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService projectService;

    // List
    @GetMapping
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_READ)")
    @Operation(summary = "Seznam projektů (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<ProjectSummaryDto>> list(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute ProjectFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal,
            HttpServletRequest req
    ) {
        if (log.isDebugEnabled()) {
            Sort s = (pageable != null ? pageable.getSort() : Sort.unsorted());
            log.debug("[Projects] QS={} filter={} rawSort={}", req.getQueryString(), filter, s);
        }
        Page<ProjectSummaryDto> page = projectService.list(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    // Detail
    @GetMapping("/{id}")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_READ)")
    @Operation(summary = "Get project by id")
    public ResponseEntity<ProjectDto> get(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        ProjectDto dto = projectService.get(id);
        return ResponseEntity.ok(dto);
    }

    // Create
    @PostMapping
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(" +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE" +   //PROJECTS_CREATE
                    ")"
    )
    @Operation(summary = "Create project")
    public ResponseEntity<ProjectDto> create(
            @PathVariable UUID companyId,
            @RequestBody @Valid CreateProjectRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        ProjectDto dto = projectService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Update (PATCH)
    @PatchMapping("/{id}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(" +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_UPDATE" +
                    ")"
    )
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectDto> update(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProjectRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        ProjectDto dto = projectService.update(id, req);
        return ResponseEntity.ok(dto);
    }

    // Delete (hard)
    @DeleteMapping("/{id}")
    @PreAuthorize("@companyGuard.sameCompany(#companyId, principal) && " +
            "@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE)")
    @Operation(summary = "Delete project")
    public ResponseEntity<Void> delete(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Archive (soft delete)
    @PostMapping("/{id}/archive")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(" +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_ARCHIVE" +
                    ")"
    )
    @Operation(summary = "Archive project (soft delete)")
    public ResponseEntity<Void> archive(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        projectService.archive(id);
        return ResponseEntity.noContent().build();
    }

    // Unarchive
    @PostMapping("/{id}/unarchive")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(" +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_ARCHIVE" +
                    ")"
    )
    @Operation(summary = "Unarchive project")
    public ResponseEntity<Void> unarchive(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        projectService.unarchive(id);
        return ResponseEntity.noContent().build();
    }

    // Upsert translation (create/update)
    @PutMapping("/{id}/translations/{locale}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(" +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_CREATE" +
                    ")"
    )
    @Operation(summary = "Upsert project translation")
    public ResponseEntity<Void> upsertTranslation(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @PathVariable String locale,
            @RequestBody @Valid UpsertProjectTranslationRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        projectService.upsertTranslation(id, locale, req);
        return ResponseEntity.noContent().build();
    }

    // Delete translation
    @DeleteMapping("/{id}/translations/{locale}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasAnyScope(" +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_WRITE, " +
                    "T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_REMOVE" +
                    ")"
    )
    @Operation(summary = "Delete project translation")
    public ResponseEntity<Void> deleteTranslation(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @PathVariable String locale,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        projectService.deleteTranslation(id, locale);
        return ResponseEntity.noContent().build();
    }
}
