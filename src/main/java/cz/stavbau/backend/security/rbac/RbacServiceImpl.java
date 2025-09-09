package cz.stavbau.backend.security.rbac;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Implementace RbacService (MVP).
 * - Pracuje s AppUserPrincipal (JWT claims: companyRole, projectRoles, scopes).
 * - Pro projektové kontroly může později volat ProjectMember repo (Sprint 3).
 */
@Service
public class RbacServiceImpl implements RbacService {

    public RbacServiceImpl() {
        // DI závislosti doplníme v implementačním PR (repo, utils).
    }

    @Override
    public boolean hasScope(String scope) {
        return false; // skeleton
    }

    @Override
    public boolean hasAnyScope(String... scopes) {
        return false; // skeleton
    }

    @Override
    public boolean hasProjectScope(UUID projectId, String scope) {
        return false; // skeleton
    }

    @Override
    public boolean canReadProject(UUID projectId) {
        return false; // skeleton
    }

    @Override
    public boolean isMemberOfProject(UUID projectId) {
        return false; // skeleton
    }

    @Override
    public Set<String> currentUserScopes() {
        return Collections.emptySet(); // skeleton
    }
}
