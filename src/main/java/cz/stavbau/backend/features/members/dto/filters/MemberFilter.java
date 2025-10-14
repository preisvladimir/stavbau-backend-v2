package cz.stavbau.backend.features.members.dto.filters;

import lombok.Getter;
import lombok.Setter;

/**
 * Vstupní filtry pro list členů (members).
 * Pozn.: String pro role/status kvůli tolerantnímu vstupu z API; převod na enum až ve Specification.
 */
@Getter @Setter
public class MemberFilter {
    /** Fulltext přes user.email, displayName/jméno, phone */
    private String q;
    /** Company role (OWNER, ADMIN, ...) – case-insensitive */
    private String role;
    /** Stav člena (ACTIVE, INVITED, DISABLED, ARCHIVED) – case-insensitive */
    private String status;
}
