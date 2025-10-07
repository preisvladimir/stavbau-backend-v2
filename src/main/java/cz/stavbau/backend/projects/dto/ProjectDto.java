package cz.stavbau.backend.projects.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import cz.stavbau.backend.projects.model.ProjectStatus;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectDto {
    private UUID id;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String code;
    private String name;          // resolved i18n
    private String description;   // resolved i18n
    private AddressDto siteAddress;
    private ProjectStatus status;
    private String statusLabel;   // připraveno na i18n label
    private UUID customerId;
    private UUID projectManagerId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private String currency;
    private String vatMode;
}
