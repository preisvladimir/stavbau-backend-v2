package cz.stavbau.backend.features.projects.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateProjectRequest {
    //@Size(min = 1, max = 32) private String code;
    @Size(max = 160) private String name;
    @Size(max = 2000) private String description;

    private UUID customerId;
    private UUID projectManagerId;

    private AddressDto siteAddress;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String currency;
    private String vatMode;
}
