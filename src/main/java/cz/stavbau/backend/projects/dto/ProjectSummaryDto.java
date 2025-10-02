package cz.stavbau.backend.projects.dto;

import cz.stavbau.backend.projects.model.ProjectStatus;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectSummaryDto {
    private UUID id;
    private String code;
    private String name; // resolved i18n
    private ProjectStatus status;
    private String statusLabel;
    private UUID customerId;
    private UUID projectManagerId;
}
