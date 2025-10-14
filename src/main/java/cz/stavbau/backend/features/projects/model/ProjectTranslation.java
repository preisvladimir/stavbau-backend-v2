package cz.stavbau.backend.features.projects.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "project_translations")
@IdClass(ProjectTranslationId.class)
public class ProjectTranslation {

    @Id @Column(name = "project_id", nullable = false)
    private java.util.UUID projectId;

    @Id @Column(name = "locale", length = 8, nullable = false)
    private String locale;

    @Column(name = "name", length = 160, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
}
