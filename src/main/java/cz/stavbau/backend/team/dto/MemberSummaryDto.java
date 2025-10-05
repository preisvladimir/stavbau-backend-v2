// src/main/java/cz/stavbau/backend/team/dto/MemberSummaryDto.java
package cz.stavbau.backend.team.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MemberSummaryDto {

    /** Primární identifikátor vazby company ↔ user. */
    private UUID id;

    /** Uživatelské ID (pro případy, kdy FE potřebuje uživatele samostatně). */
    private UUID userId;

    /** E-mail preferenčně z User; může být null (např. pozvánka bez dokončení). */
    private String email;

    /** Jméno/příjmení – preferenčně z CompanyMember, jinak fallback na User. */
    private String firstName;
    private String lastName;

    /** Přátelské zobrazení jména pro FE (fallback: "First Last" | email). */
    private String displayName;

    /** Volitelný kontakt uložený u membera. */
    private String phone;

    /** Kanonická role v rámci company (OWNER|ADMIN|EDITOR|VIEWER… jako String/Enum name). */
    private String companyRole;

    /** ⚠️ Kompat alias pro starší FE – duplicitně drží stejnou hodnotu jako companyRole. */
    @Deprecated
    private String role;

    /** Volitelně status člena (pokud model máte, např. INVITED/ACTIVE). */
    private String status;

    /** Audit (hodí se na klientu). */
    private Instant createdAt;
    private Instant updatedAt;
}
