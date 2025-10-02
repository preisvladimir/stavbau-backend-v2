package cz.stavbau.backend.projects.service;

import cz.stavbau.backend.projects.dto.*;
import org.springframework.data.domain.*;
import java.util.UUID;

public interface ProjectService {
    ProjectDto create(CreateProjectRequest request);
    ProjectDto update(UUID id, UpdateProjectRequest request);
    void delete(UUID id); // v PR 3/4 použijeme spíš archive endpoint, tady jen skeleton
    ProjectDto get(UUID id);
    Page<ProjectSummaryDto> list(String q, Pageable pageable);
}
