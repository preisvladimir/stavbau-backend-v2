package cz.stavbau.backend.security.rbac;

import java.util.*;

import static cz.stavbau.backend.security.rbac.Scopes.*;
import static java.util.Map.entry;

/**
 * MVP in-code role→scopes (RBAC 2.1 §5.1, §3.1/3.2).
 * PRO fáze: perzistence v DB (RBAC 2.1 §5.2).
 */
public final class BuiltInRoles {

    private BuiltInRoles() {}

    // --- Team (MVP) – agregované scopy ---
    // POZN.: v Scopes nemáme TEAM_WRITE; "write" znamená add/remove/update_role.
    private static final Set<String> TEAM_RW = Set.of(TEAM_READ, TEAM_ADD, TEAM_REMOVE, TEAM_UPDATE, TEAM_UPDATE_ROLE);
    private static final Set<String> TEAM_R  = Set.of(TEAM_READ);

    private static final Set<String> SUPERADMIN_BASE = Set.of(
            DASHBOARD_VIEW,
            TEAM_WRITE,
            PROJECTS_READ, PROJECTS_CREATE, PROJECTS_UPDATE, PROJECTS_DELETE, PROJECTS_ARCHIVE, PROJECTS_ASSIGN,
            LOGBOOK_READ, LOGBOOK_CREATE, LOGBOOK_UPDATE, LOGBOOK_DELETE, LOGBOOK_EXPORT,
            BUDGET_READ, BUDGET_CREATE, BUDGET_UPDATE, BUDGET_DELETE, BUDGET_APPROVE, BUDGET_EXPORT,
            FILES_READ, FILES_UPLOAD, FILES_UPDATE, FILES_DELETE, FILES_DOWNLOAD, FILES_SHARE,
            ADMIN_USERS_READ, ADMIN_USERS_MANAGE, INTEGRATIONS_MANAGE
    );

    // Základní „admin“ balík – jak jsme měli dříve (můžeš zúžit podle potřeby)
    private static final Set<String> ADMINISTRATION_BASE = Set.of(
            DASHBOARD_VIEW,
            PROJECTS_READ, PROJECTS_CREATE, PROJECTS_UPDATE, PROJECTS_DELETE, PROJECTS_ARCHIVE, PROJECTS_ASSIGN,
            LOGBOOK_READ, LOGBOOK_CREATE, LOGBOOK_UPDATE, LOGBOOK_DELETE, LOGBOOK_EXPORT,
            BUDGET_READ, BUDGET_CREATE, BUDGET_UPDATE, BUDGET_DELETE, BUDGET_APPROVE, BUDGET_EXPORT,
            FILES_READ, FILES_UPLOAD, FILES_UPDATE, FILES_DELETE, FILES_DOWNLOAD, FILES_SHARE,
            TEAM_READ, TEAM_ADD, TEAM_REMOVE, TEAM_UPDATE_ROLE, TEAM_UPDATE,
            ADMIN_USERS_READ, ADMIN_USERS_MANAGE, INTEGRATIONS_MANAGE
    );



    @SafeVarargs
    private static Set<String> union(Set<String>... sets) {
        Set<String> out = new HashSet<>();
        for (Set<String> s : sets) out.addAll(s);
        return Collections.unmodifiableSet(out);
    }

    /** Company role → scopes (MVP). */
    public static final Map<CompanyRoleName, Set<String>> COMPANY_ROLE_SCOPES = Map.ofEntries(
            entry(CompanyRoleName.SUPERADMIN,  union(SUPERADMIN_BASE)),
            entry(CompanyRoleName.OWNER,       union(ADMINISTRATION_BASE)),
            // Pokud chceš mít COMPANY_ADMIN omezenější než OWNER, zúžím: např. bez BUDGET_APPROVE / ADMIN_USERS_MANAGE
            entry(CompanyRoleName.COMPANY_ADMIN, union(ADMINISTRATION_BASE)),
            // Lehká váha – jen teamové operace (MVP varianta)
            entry(CompanyRoleName.HR_MANAGER,  TEAM_RW),
            entry(CompanyRoleName.AUDITOR_READONLY, TEAM_R),
            entry(CompanyRoleName.VIEWER,      TEAM_R),

            // Ostatní role – zatím bez team pravomocí v MVP (necháváme prázdné sety, ať je to explicitní)
            entry(CompanyRoleName.ACCOUNTANT,  Set.of()),
            entry(CompanyRoleName.PURCHASING,  Set.of()),
            entry(CompanyRoleName.DOC_CONTROLLER, Set.of()),
            entry(CompanyRoleName.FLEET_MANAGER,  Set.of()),
            entry(CompanyRoleName.INTEGRATION, Set.of()),
            // POZN.: pokud v enumu nemáš MEMBER a používáme MANAGER, změň to na MANAGER:
            // entry(CompanyRoleName.MANAGER, Set.of())
            entry(CompanyRoleName.MANAGER, Set.of())
    );

    /** Project role → scopes (Sprint 3; zatím prázdné). */
    public static final Map<ProjectRoleName, Set<String>> PROJECT_ROLE_SCOPES = Map.of();
}
