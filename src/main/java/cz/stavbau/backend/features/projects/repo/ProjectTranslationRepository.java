package cz.stavbau.backend.features.projects.repo;

import cz.stavbau.backend.features.projects.model.ProjectTranslation;
import cz.stavbau.backend.features.projects.model.ProjectTranslationId;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTranslationRepository extends JpaRepository<ProjectTranslation, ProjectTranslationId> {
    List<ProjectTranslation> findByProjectId(UUID projectId);

    // preferovaný jazyk
    List<ProjectTranslation> findByProjectIdInAndLocale(Collection<UUID> projectIds, String locale);

    // fallback: vezmeme všechny překlady pro dané projekty a vybereme 1 v Javě
    List<ProjectTranslation> findByProjectIdIn(Collection<UUID> projectIds);
}
