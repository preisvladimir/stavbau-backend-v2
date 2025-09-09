package cz.stavbau.backend.security.rbac;

import java.util.UUID;

/** Přiřazení projektové role ke konkrétnímu projektu (pro JWT/Principal). */
public record ProjectRoleAssignment(UUID projectId, ProjectRoleName role) {}
