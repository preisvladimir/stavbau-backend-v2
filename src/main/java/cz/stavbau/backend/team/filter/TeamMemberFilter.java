// src/main/java/cz/stavbau/backend/team/filter/TeamMemberFilter.java
package cz.stavbau.backend.team.filter;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeamMemberFilter {
    /** Fulltext přes user.email, firstName/lastName, phone */
    private String q;
    /** Company role (enum/string dle modelu) – např. OWNER, ADMIN… */
    private String role;
    /** Volitelně – user state/status, pokud používáš (např. ACTIVE, INVITED…) */
    private String status;
}
