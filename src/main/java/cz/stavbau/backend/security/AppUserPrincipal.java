package cz.stavbau.backend.security;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.security.rbac.ProjectRoleAssignment;

import java.io.Serializable;
import java.util.*;

/**
 * Principal nesený v SecurityContextu.
 * Jediný zdroj pravdy pro FE toggly (role/scopes) a tenancy (companyId).
 */
public class AppUserPrincipal implements Serializable {

    private UUID userId;
    private UUID companyId;
    private String email;

    private CompanyRoleName companyRole;                 // může být null
    private List<ProjectRoleAssignment> projectRoles;    // může být prázdné
    private Set<String> scopes;                          // může být prázdné

    public AppUserPrincipal() {
        this.projectRoles = List.of();
        this.scopes = Set.of();
    }

    public AppUserPrincipal(UUID userId, UUID companyId, String email,
                            CompanyRoleName companyRole,
                            List<ProjectRoleAssignment> projectRoles,
                            Set<String> scopes) {
        this.userId = userId;
        this.companyId = companyId;
        this.email = email;
        this.companyRole = companyRole;
        this.projectRoles = projectRoles != null ? List.copyOf(projectRoles) : List.of();
        this.scopes = scopes != null ? Set.copyOf(scopes) : Set.of();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public CompanyRoleName getCompanyRole() { return companyRole; }
    public void setCompanyRole(CompanyRoleName companyRole) { this.companyRole = companyRole; }

    public List<ProjectRoleAssignment> getProjectRoles() { return projectRoles; }
    public void setProjectRoles(List<ProjectRoleAssignment> projectRoles) {
        this.projectRoles = projectRoles != null ? List.copyOf(projectRoles) : List.of();
    }

    public Set<String> getScopes() { return scopes; }
    public void setScopes(Set<String> scopes) { this.scopes = scopes != null ? Set.copyOf(scopes) : Set.of(); }

    // Pohodlné helpers (může využít RbacService)
    public boolean hasScope(String scope) { return scopes != null && scopes.contains(scope); }

    @Override public String toString() {
        return "AppUserPrincipal{userId=%s, companyId=%s, email=%s, companyRole=%s, scopes=%d}"
                .formatted(userId, companyId, email, companyRole, scopes != null ? scopes.size() : 0);
    }
}
