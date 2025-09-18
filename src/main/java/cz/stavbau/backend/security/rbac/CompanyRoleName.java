package cz.stavbau.backend.security.rbac;

/** Company-level role names (RBAC 2.1 §3.1). */
public enum CompanyRoleName {
    OWNER,
    COMPANY_ADMIN,
    ACCOUNTANT,
    PURCHASING,
    DOC_CONTROLLER,
    FLEET_MANAGER,
    HR_MANAGER,
    AUDITOR_READONLY,
    INTEGRATION,
    MEMBER,
    VIEWER,
    SUPERADMIN
}
