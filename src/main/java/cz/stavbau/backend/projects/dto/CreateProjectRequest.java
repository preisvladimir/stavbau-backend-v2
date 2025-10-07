package cz.stavbau.backend.projects.dto;

import cz.stavbau.backend.common.api.dto.AddressDto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateProjectRequest {
    //@Size(min = 1, max = 32) private String code; // může být generován později
    @NotBlank @Size(max = 160) private String name;
    @Size(max = 2000) private String description;

    @NotNull private UUID customerId;
    private UUID projectManagerId;

    private AddressDto siteAddress;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String currency; // default from company; lze nechat null
    private String vatMode;  // default from company; lze nechat null

    // siteAddressJson a další pole přidáme v dalším PR při DTO rozšíření
}
