package cz.stavbau.backend.security.rbac;

import java.util.*;
import static java.util.Map.entry;

/**
 * MVP in-code role→scopes (RBAC 2.1 §5.1, §3.1/3.2).
 * PRO fáze: perzistence v DB (RBAC 2.1 §5.2).
 */
public final class BuiltInRoles {

    private BuiltInRoles() {}

    // --- Scopes z jiných modulů (ponecháváme zde jako jednoduché konstanty) ---
    public static final Set<String> INVOICE_SCOPES = Set.of(
            "invoices:read","invoices:write","invoices:delete","invoices:export"
    );
    public static final Set<String> FILE_SCOPES = Set.of(
            "files:read","files:write","files:delete","files:tag"
    );

    // --- Team (MVP) – agregované scopy ---
    private static final Set<String> TEAM_RW = Set.of(Scopes.TEAM_READ, Scopes.TEAM_WRITE);
    private static final Set<String> TEAM_R  = Set.of(Scopes.TEAM_READ);

    private static Set<String> union(Set<String>... sets) {
        Set<String> out = new HashSet<>();
        for (Set<String> s : sets) out.addAll(s);
        return Collections.unmodifiableSet(out);
    }

    /** Company role → scopes (MVP). */
    public static final Map<CompanyRoleName, Set<String>> COMPANY_ROLE_SCOPES = Map.ofEntries(
            // OWNER: vše z admina + team:* (MVP = read+write). Viz RBAC 2.1 „OWNER → vše + team:*“. :contentReference[oaicite:1]{index=1}
            entry(CompanyRoleName.OWNER, union(TEAM_RW /*, další company/global scopy dle potřeby */)),

            // COMPANY_ADMIN: team:* (MVP = read+write). :contentReference[oaicite:2]{index=2}
            entry(CompanyRoleName.COMPANY_ADMIN, TEAM_RW),

            // VIEWER: read-only (včetně team:read). :contentReference[oaicite:3]{index=3}
            entry(CompanyRoleName.VIEWER, TEAM_R),

            // AUDITOR_READONLY: *:read – v MVP explicitně přidáme team:read. :contentReference[oaicite:4]{index=4}
            entry(CompanyRoleName.AUDITOR_READONLY, TEAM_R),

            // Ostatní role zatím bez team pravomocí v MVP:
            entry(CompanyRoleName.ACCOUNTANT, Set.of()),
            entry(CompanyRoleName.PURCHASING, Set.of()),
            entry(CompanyRoleName.DOC_CONTROLLER, Set.of()),
            entry(CompanyRoleName.FLEET_MANAGER, Set.of()),
            entry(CompanyRoleName.HR_MANAGER, Set.of()),
            entry(CompanyRoleName.INTEGRATION, Set.of()),
            entry(CompanyRoleName.MEMBER, Set.of()),
            // SUPERADMIN – provozní all-access; v MVP necháme prázdné a řešíme separátně v SecurityConfig. :contentReference[oaicite:5]{index=5}
            entry(CompanyRoleName.SUPERADMIN, Set.of())
    );

    /** Project role → scopes (připravíme pro Sprint 3 – zatím prázdné). */
    public static final Map<ProjectRoleName, Set<String>> PROJECT_ROLE_SCOPES = Map.of(
            // bude doplněno v kroku se zavedením Project/ProjectMember
    );
}
