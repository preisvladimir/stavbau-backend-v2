package cz.stavbau.backend.features.projects.dto;

import cz.stavbau.backend.features.projects.model.ProjectStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import cz.stavbau.backend.features.projects.model.ProjectType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectSummaryDto {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID projectManagerId;
    private String projectManagerName;
    private String code;
    private String name;              // kanonické
    private String nameLocalized;     // resolved overlay
    private ProjectStatus status;
    private ProjectType type;         // ← nově
    private Instant createdAt;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String currency;
    private BigDecimal contractValueNet;   // ← volitelné
}