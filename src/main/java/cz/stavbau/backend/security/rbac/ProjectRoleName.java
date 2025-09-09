package cz.stavbau.backend.security.rbac;

/** Project-level role names (RBAC 2.1 §3.2). */
public enum ProjectRoleName {
    PROJECT_MANAGER,
    SITE_MANAGER,     // stavbyvedoucí
    FOREMAN,          // mistr
    QS,               // rozpočtář
    HSE,              // BOZP
    DESIGNER,
    SUBCONTRACTOR,
    CLIENT,
    PROJECT_VIEWER
}
