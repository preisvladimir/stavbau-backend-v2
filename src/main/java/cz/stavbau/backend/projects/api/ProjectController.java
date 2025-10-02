package cz.stavbau.backend.projects.api;

import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.service.ProjectService;
import cz.stavbau.backend.common.i18n.LocaleResolver;
import cz.stavbau.backend.security.rbac.Scopes;
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
public class ProjectController {

    private final ProjectService service;
    private final LocaleResolver localeResolver;

    private HttpHeaders i18nHeaders(Locale locale) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        h.setVary(java.util.List.of(HttpHeaders.ACCEPT_LANGUAGE));
        return h;
    }

    @GetMapping
    @PreAuthorize("@rbac.hasScope('projects:read')")
    public ResponseEntity<Page<ProjectSummaryDto>> list(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="size", defaultValue="20") int size,
            @RequestParam(value="sort", defaultValue="code,asc") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort.split(",")));
        var result = service.list(q, pageable);
        return new ResponseEntity<>(result, i18nHeaders(localeResolver.resolve()), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:read')")
    public ResponseEntity<ProjectDto> get(@PathVariable UUID id) {
        var dto = service.get(id);
        return new ResponseEntity<>(dto, i18nHeaders(localeResolver.resolve()), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("@rbac.hasScope('projects:create')")
    public ResponseEntity<ProjectDto> create(@RequestBody @jakarta.validation.Valid CreateProjectRequest req) {
        var dto = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/v1/projects/" + dto.getId()))
                .headers(i18nHeaders(localeResolver.resolve()))
                .body(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:update')")
    public ResponseEntity<ProjectDto> update(@PathVariable UUID id, @RequestBody @jakarta.validation.Valid UpdateProjectRequest req) {
        var dto = service.update(id, req);
        return new ResponseEntity<>(dto, i18nHeaders(localeResolver.resolve()), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbac.hasScope('projects:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // TODO (další PR): POST /{id}/archive, POST/DELETE members
}
