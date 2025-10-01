package cz.stavbau.backend.team.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembersStatsDto {
    private long owners;
    private long active;
    private long invited;
    private long disabled;
    private long total;
}