package cz.stavbau.backend.projects.api;

import cz.stavbau.backend.common.api.PageResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService service;
    private final I18nLocaleService i18nLocale;

    private HttpHeaders i18nHeaders(java.util.Locale locale) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        h.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
        return h;
    }

    @GetMapping
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).PROJECTS_READ)")
    public ResponseEntity<PageResponse<ProjectSummaryDto>> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Pageable pageable,
            Locale locale
    ) {
        // Bezpečný sort + fallback + allow-list
        var allowed = Set.of("createdAt", "code", "translations.name");
        Sort sort = PageableUtils.safeSortOrDefault(pageable, Sort.by("createdAt").descending(), allowed);
        var paging = PageRequest.of(Math.max(page, 0), Math.min(size, 100), sort);

        var pageData = service.list(q, paging, locale);
        var body = PageResponse.of(pageData);

        return ResponseEntity.ok()
                .header(HttpHeaders.VARY, "Accept-Language")
                .header(HttpHeaders.CONTENT_LANGUAGE, locale != null ? locale.toLanguageTag() : "cs")
                .body(body);
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
