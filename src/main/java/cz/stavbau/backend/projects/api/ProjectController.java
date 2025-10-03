package cz.stavbau.backend.projects.api;

import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.projects.dto.CreateProjectRequest;
import cz.stavbau.backend.projects.dto.ProjectDto;
import cz.stavbau.backend.projects.dto.ProjectSummaryDto;
import cz.stavbau.backend.projects.dto.UpdateProjectRequest;
import cz.stavbau.backend.projects.dto.ProjectMemberRequest;
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
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService service;
    private final I18nLocaleService i18nLocale;

    private HttpHeaders i18nHeaders(Locale locale) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        h.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
        return h;
    }

    private Pageable pageable(String sort, int page, int size) {
        Sort s;
        if (sort != null && sort.contains(",")) {
            String[] p = sort.split(",", 2);
            s = Sort.by(new Sort.Order(Sort.Direction.fromString(p[1]), p[0]));
        } else if (sort != null && !sort.isBlank()) {
            s = Sort.by(sort).ascending();
        } else {
            s = Sort.by("code").ascending();
        }
        return PageRequest.of(page, size, s);
    }

    @GetMapping
    @PreAuthorize("@rbac.hasScope('projects:read')")
    @Operation(summary = "List projects (paged)")
    public ResponseEntity<Page<ProjectSummaryDto>> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "code,asc") String sort
    ) {
        var loc = i18nLocale.resolve();
        var result = service.list(q, pageable(sort, page, size));
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
    public ResponseEntity<ProjectDto> create(@RequestBody @Valid CreateProjectRequest req) {
        var loc = i18nLocale.resolve();
        var dto = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/v1/projects/" + dto.getId()))
                .headers(i18nHeaders(loc))
                .body(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:update')")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectDto> update(@PathVariable UUID id, @RequestBody @Valid UpdateProjectRequest req) {
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

    // --- NEW: archive (soft delete) ---
    @PostMapping("/{id}/archive")
    @PreAuthorize("@rbac.hasScope('projects:delete')")
    @Operation(summary = "Archive project (soft delete)")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        var loc = i18nLocale.resolve();
        service.archive(id);
        return ResponseEntity.noContent().headers(i18nHeaders(loc)).build();
    }

    // --- NEW: members (stubs, bez logiky) ---
    @PostMapping("/{id}/members")
    @PreAuthorize("@rbac.hasScope('projects:assign')")
    @Operation(summary = "Assign member to project (stub)")
    public ResponseEntity<Void> addMember(@PathVariable UUID id, @RequestBody @Valid ProjectMemberRequest req) {
        var loc = i18nLocale.resolve();
        // TODO: implement in next PR (service call assignMember)
        return ResponseEntity.accepted().headers(i18nHeaders(loc)).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("@rbac.hasScope('projects:assign')")
    @Operation(summary = "Remove member from project (stub)")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        var loc = i18nLocale.resolve();
        // TODO: implement in next PR (service call removeMember)
        return ResponseEntity.noContent().headers(i18nHeaders(loc)).build();
    }
}
