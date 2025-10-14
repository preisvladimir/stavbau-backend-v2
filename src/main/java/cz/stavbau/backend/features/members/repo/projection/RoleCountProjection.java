// src/main/java/cz/stavbau/backend/team/repo/projection/RoleCountProjection.java
package cz.stavbau.backend.features.members.repo.projection;

import cz.stavbau.backend.security.rbac.CompanyRoleName;

public interface RoleCountProjection {
    CompanyRoleName getRole();
    Long getCnt();
}
