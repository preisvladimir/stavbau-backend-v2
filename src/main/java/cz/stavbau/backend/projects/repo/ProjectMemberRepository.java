package cz.stavbau.backend.projects.repo;

import cz.stavbau.backend.projects.model.ProjectMember;
import cz.stavbau.backend.projects.model.ProjectMemberId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProjectId(UUID projectId);
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
}
