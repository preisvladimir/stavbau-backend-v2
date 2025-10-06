package cz.stavbau.backend.projects.api;

import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.common.persistence.PageableUtils;
import cz.stavbau.backend.projects.dto.ProjectDto;
import cz.stavbau.backend.projects.dto.ProjectSummaryDto;
import cz.stavbau.backend.projects.filter.ProjectFilter;
import cz.stavbau.backend.projects.model.ProjectStatus;
import cz.stavbau.backend.projects.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService service;
    private final I18nLocaleService i18nLocale;

    private HttpHeaders i18nHeaders(java.util.Locale locale) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        h.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
        return h;
    }

    @GetMapping
    @PreAuthorize("@rbac.hasScope('projects:read')")
    @Operation(summary = "List projects (paged)")
    public ResponseEntity<Page<ProjectSummaryDto>> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "status", required = false) ProjectStatus status,
            @RequestParam(value = "customerId", required = false) UUID customerId,
            @RequestParam(value = "projectManagerId", required = false) UUID projectManagerId,
            @RequestParam(value = "active", required = false) Boolean active,
            // date ranges (volitelné)
            @RequestParam(value = "plannedStartFrom", required = false) java.time.LocalDate plannedStartFrom,
            @RequestParam(value = "plannedStartTo",   required = false) java.time.LocalDate plannedStartTo,
            @RequestParam(value = "plannedEndFrom",   required = false) java.time.LocalDate plannedEndFrom,
            @RequestParam(value = "plannedEndTo",     required = false) java.time.LocalDate plannedEndTo,
            // paging/sort
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "code,asc") String sort
    ) {
        var loc = i18nLocale.resolve();

        // Povolené properties podle entity Project; alias "name" → "code" (pro UX kompatibilitu FE)
        Pageable pageable = PageableUtils.from(
                sort, page, size,
                /* default */ "code",
                /* allowed */ Set.of("code","status","createdAt","updatedAt",
                        "plannedStartDate","plannedEndDate","actualStartDate","actualEndDate","archivedAt"),
                /* aliases */ Map.of("name","code")
        );

        var f = new ProjectFilter();
        f.setQ(q);
        f.setCode(code);
        f.setStatus(status);
        f.setCustomerId(customerId);
        f.setProjectManagerId(projectManagerId);
        f.setActive(active);
        f.setPlannedStartFrom(plannedStartFrom);
        f.setPlannedStartTo(plannedStartTo);
        f.setPlannedEndFrom(plannedEndFrom);
        f.setPlannedEndTo(plannedEndTo);

        var result = service.list(f, pageable);
        return new ResponseEntity<>(result, i18nHeaders(loc), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:read')")
    @Operation(summary = "Get project by id")
    public ResponseEntity<ProjectDto> get(@PathVariable UUID id) {
        var loc = i18nLocale.resolve();
        var dto = service.get(id);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("@rbac.hasScope('projects:create')")
    @Operation(summary = "Create project")
    public ResponseEntity<ProjectDto> create(@RequestBody @Valid cz.stavbau.backend.projects.dto.CreateProjectRequest req) {
        var loc = i18nLocale.resolve();
        var dto = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + dto.getId()))
                .headers(i18nHeaders(loc))
                .body(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:update')")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectDto> update(@PathVariable UUID id, @RequestBody @Valid cz.stavbau.backend.projects.dto.UpdateProjectRequest req) {
        var loc = i18nLocale.resolve();
        var dto = service.update(id, req);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:delete')")
    @Operation(summary = "Delete project (temporary hard delete; will be replaced by archive)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Archive (soft delete)
    @PostMapping("/{id}/archive")
    @PreAuthorize("@rbac.hasScope('projects:archive')")
    @Operation(summary = "Archive project (soft delete)")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        var loc = i18nLocale.resolve();
        service.archive(id);
        return ResponseEntity.noContent().headers(i18nHeaders(loc)).build();
    }
}
