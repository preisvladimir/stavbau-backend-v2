package cz.stavbau.backend.features.projects.model;

import cz.stavbau.backend.security.rbac.ProjectRoleName;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "project_members")
@IdClass(ProjectMemberId.class)
public class ProjectMember {

    @Id @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Id @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private ProjectRoleName role;
}

