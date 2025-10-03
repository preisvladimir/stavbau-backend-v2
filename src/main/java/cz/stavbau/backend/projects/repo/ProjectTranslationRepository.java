package cz.stavbau.backend.projects.repo;

import cz.stavbau.backend.projects.model.ProjectTranslation;
import cz.stavbau.backend.projects.model.ProjectTranslationId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTranslationRepository extends JpaRepository<ProjectTranslation, ProjectTranslationId> {
    List<ProjectTranslation> findByProjectId(UUID projectId);
}
