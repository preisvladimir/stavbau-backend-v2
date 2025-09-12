package cz.stavbau.backend.security.rbac;

import java.util.Map;
import java.util.Set;

/**
 * MVP in-code role→scopes (RBAC 2.1 §5.1, §3.1/3.2).
 * PRO fáze: perzistence v DB (RBAC 2.1 §5.2).
 */
public final class BuiltInRoles {

    private BuiltInRoles() {}

    /** Company role → scopes (MVP). */
    public static final Map<CompanyRoleName, Set<String>> COMPANY_ROLE_SCOPES = Map.of(
            // Naplníme dle RBAC 2.1 – skeleton ponechán prázdný pro PR (bez implementace).
    );

    /** Project role → scopes (připravíme pro Sprint 3 – zatím prázdné). */
    public static final Map<ProjectRoleName, Set<String>> PROJECT_ROLE_SCOPES = Map.of(
            // bude doplněno v kroku se zavedením Project/ProjectMember
    );

    Set<String> INVOICE_SCOPES = Set.of("invoices:read","invoices:write","invoices:delete","invoices:export");
    Set<String> FILE_SCOPES = Set.of("files:read","files:write","files:delete","files:tag");
}
