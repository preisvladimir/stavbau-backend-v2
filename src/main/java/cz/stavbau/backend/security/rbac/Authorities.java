package cz.stavbau.backend.security.rbac;

/** Pomocné převody na Spring Security authorities. */
public final class Authorities {

    private Authorities() {}

    /** "projects:read" -> "SCOPE_projects:read" */
    public static String toScopeAuthority(String scope) {
        return "SCOPE_" + scope;
    }

    /** "OWNER" -> "ROLE_OWNER" (užitečné pro ladění/diagnostiku). */
    public static String toRoleAuthority(CompanyRoleName role) {
        return "ROLE_" + role.name();
    }
}
