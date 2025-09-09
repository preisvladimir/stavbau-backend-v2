package cz.stavbau.backend.security.rbac;

/**
 * Jediné místo s textovými konstantami scopes (RBAC 2.1 §2).
 * Formát: "area:action" (lowercase).
 */
public final class Scopes {

    private Scopes() {}

    // Dashboard
    public static final String DASHBOARD_VIEW = "dashboard:view";

    // Projects
    public static final String PROJECTS_READ   = "projects:read";
    public static final String PROJECTS_CREATE = "projects:create";
    public static final String PROJECTS_UPDATE = "projects:update";
    public static final String PROJECTS_DELETE = "projects:delete";
    public static final String PROJECTS_ARCHIVE = "projects:archive";
    public static final String PROJECTS_ASSIGN  = "projects:assign";

    // Logbook (Deník) – výběr
    public static final String LOGBOOK_READ   = "logbook:read";
    public static final String LOGBOOK_CREATE = "logbook:create";
    public static final String LOGBOOK_UPDATE = "logbook:update";
    public static final String LOGBOOK_DELETE = "logbook:delete";
    public static final String LOGBOOK_EXPORT = "logbook:export";

    // Budget – výběr
    public static final String BUDGET_READ    = "budget:read";
    public static final String BUDGET_CREATE  = "budget:create";
    public static final String BUDGET_UPDATE  = "budget:update";
    public static final String BUDGET_DELETE  = "budget:delete";
    public static final String BUDGET_APPROVE = "budget:approve";
    public static final String BUDGET_EXPORT  = "budget:export";

    // Files – výběr
    public static final String FILES_READ     = "files:read";
    public static final String FILES_UPLOAD   = "files:upload";
    public static final String FILES_UPDATE   = "files:update";
    public static final String FILES_DELETE   = "files:delete";
    public static final String FILES_DOWNLOAD = "files:download";
    public static final String FILES_SHARE    = "files:share";

    // Team – výběr
    public static final String TEAM_READ       = "team:read";
    public static final String TEAM_ADD        = "team:add";
    public static final String TEAM_REMOVE     = "team:remove";
    public static final String TEAM_UPDATE_ROLE= "team:update_role";

    // Admin / Integrations – výběr
    public static final String ADMIN_USERS_READ   = "admin:users_read";
    public static final String ADMIN_USERS_MANAGE = "admin:users_manage";
    public static final String INTEGRATIONS_MANAGE = "integrations:manage";

    // ...doplníme zbylé scopes přesně dle RBAC_2.1 (kap. 2).
}
