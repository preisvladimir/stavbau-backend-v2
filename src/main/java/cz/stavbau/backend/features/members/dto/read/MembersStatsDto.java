// src/main/java/cz/stavbau/backend/team/dto/MembersStatsDto.java
package cz.stavbau.backend.features.members.dto.read;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import lombok.*;

import java.util.Map;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MembersStatsDto {
    private long owners;
    private long active;
    private long invited;
    private long disabled;
    private long archived;
    private long total;
    private Map<CompanyRoleName, Long> byRole;
}
