package cz.stavbau.backend.features.projects.dto;

import cz.stavbau.backend.security.rbac.ProjectRoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class ProjectMemberRequest {
    @NotNull private UUID userId;
    @NotNull private ProjectRoleName role;
}
