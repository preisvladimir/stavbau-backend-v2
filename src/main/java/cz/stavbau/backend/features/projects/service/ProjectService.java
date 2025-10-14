package cz.stavbau.backend.features.projects.service;

import cz.stavbau.backend.features.projects.dto.*;
import cz.stavbau.backend.features.projects.filter.ProjectFilter;
import org.springframework.data.domain.*;

import java.util.UUID;

public interface ProjectService {
    Page<ProjectSummaryDto> list(ProjectFilter filter, Pageable pageable);
    ProjectDto get(UUID id);
    ProjectDto create(CreateProjectRequest request);
    ProjectDto update(UUID id, UpdateProjectRequest request);
    void delete(UUID id);

    void archive(UUID id);
    void unarchive(UUID id);   // přidané

    // i18n translations
    void upsertTranslation(UUID id, String locale, UpsertProjectTranslationRequest req);
    void deleteTranslation(UUID id, String locale);
}
