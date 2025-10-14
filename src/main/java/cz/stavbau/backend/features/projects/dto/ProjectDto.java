package cz.stavbau.backend.features.projects.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import cz.stavbau.backend.features.projects.model.ProjectStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import cz.stavbau.backend.features.projects.model.ProjectType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectDto {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID projectManagerId;
    private String projectManagerName;
    private String code;
    // kanonické (vždy)
    private String name;
    private String description;

    // lokalizované (resolved overlay — volitelné)
    private String nameLocalized;
    private String descriptionLocalized;

    private ProjectStatus status;
    private ProjectType type;
    private Instant createdAt;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private String currency;
    private String vatMode;
    private BigDecimal contractValueNet;
    private BigDecimal contractValueGross;
    private BigDecimal retentionPercent;
    private Integer paymentTermsDays;
    private String externalRef;
    private AddressDto siteAddress;
    private String[] tags;
}
