package cz.stavbau.backend.projects.service;

import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.filter.ProjectFilter;
import org.springframework.data.domain.*;

import java.util.Locale;
import java.util.UUID;

public interface ProjectService {
    ProjectDto create(CreateProjectRequest request);
    ProjectDto update(UUID id, UpdateProjectRequest request);
    void delete(UUID id); // v PR 3/4 použijeme spíš archive endpoint, tady jen skeleton
    void archive(UUID id);
    ProjectDto get(UUID id);


    /** Nové: typovaný filtr (doporučeno používat interně) */
    Page<ProjectSummaryDto> list(String q, Pageable pageable);
    Page<ProjectSummaryDto> list(String q, Pageable pageable,Locale locale);
    Page<ProjectSummaryDto> list(ProjectFilter filter, Pageable pageable);
    Page<ProjectSummaryDto> list(ProjectFilter filter, Pageable pageable,Locale locale);

}
