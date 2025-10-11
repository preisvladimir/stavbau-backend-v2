// src/main/java/cz/stavbau/backend/team/repo/projection/RoleCountTuple.java
package cz.stavbau.backend.team.repo.projection;

import cz.stavbau.backend.security.rbac.CompanyRoleName;

public interface RoleCountTuple {
    CompanyRoleName getRole();
    Long getCnt();
}
