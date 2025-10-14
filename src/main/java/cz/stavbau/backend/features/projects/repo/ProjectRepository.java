package cz.stavbau.backend.features.projects.repo;

import cz.stavbau.backend.features.projects.model.Project;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
    boolean existsByCompanyIdAndCode(UUID companyId, String code);
    boolean existsByCompanyIdAndProjectManagerId(UUID companyId, UUID projectManagerId);
}
