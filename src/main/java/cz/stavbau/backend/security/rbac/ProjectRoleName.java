package cz.stavbau.backend.security.rbac;

/** Project-level role names (RBAC 2.1 §3.2). */
public enum ProjectRoleName {
    PROJECT_MANAGER,
    SITE_MANAGER,     // stavbyvedoucí
    FOREMAN,          // mistr
    QUANTITY_SURVEYOR, // rozpočtář (QS)
    QS,               // rozpočtář
    HSE,              // BOZP
    DESIGNER,
    SUBCONTRACTOR,
    CLIENT,
    MEMBER,            // běžný člen projektu
    VIEWER,             // jen pro čtení
    PROJECT_VIEWER
}
