// src/main/java/cz/stavbau/backend/team/dto/TeamStatsDto.java
package cz.stavbau.backend.team.dto;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import lombok.*;

import java.util.Map;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class TeamStatsDto {
    private long owners;
    private long active;
    private long invited;
    private long disabled;
    private long archived;
    private long total;
    private Map<CompanyRoleName, Long> byRole;
}
