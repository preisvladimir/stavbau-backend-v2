package cz.stavbau.backend.security.rbac;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Bean pro SpEL v @PreAuthorize – jméno "rbac".
 * Příklad: @PreAuthorize("@rbac.hasScope('projects:read')")
 */
@Component("rbac")
public class RbacSpelEvaluator {

    private final RbacService rbac;

    public RbacSpelEvaluator(RbacService rbac) {
        this.rbac = rbac;
    }

    public boolean hasScope(String scope) {
        return rbac.hasScope(scope);
    }

    public boolean hasAnyScope(String... scopes) {
        return rbac.hasAnyScope(scopes);
    }

    public boolean hasProjectScope(UUID projectId, String scope) {
        return rbac.hasProjectScope(projectId, scope);
    }

    public boolean canReadProject(UUID projectId) {
        return rbac.canReadProject(projectId);
    }

    public boolean isMemberOfProject(UUID projectId) {
        return rbac.isMemberOfProject(projectId);
    }
}
