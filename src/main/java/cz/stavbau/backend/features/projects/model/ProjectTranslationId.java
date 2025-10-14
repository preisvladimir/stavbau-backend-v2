package cz.stavbau.backend.features.projects.model;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProjectTranslationId implements Serializable {
    private UUID projectId;
    private String locale;
}
