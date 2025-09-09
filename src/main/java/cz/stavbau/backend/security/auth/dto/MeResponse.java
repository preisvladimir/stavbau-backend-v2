package cz.stavbau.backend.security.auth.dto;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.security.rbac.ProjectRoleAssignment;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** DTO pro /auth/me – sjednocený payload pro FE (toggly & guards). */
public class MeResponse {
    public UUID id;
    public UUID companyId;
    public String email;

    public CompanyRoleName companyRole;
    public List<ProjectRoleAssignment> projectRoles;
    public Set<String> scopes;
}
