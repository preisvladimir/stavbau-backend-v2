package cz.stavbau.backend.security.rbac;

import cz.stavbau.backend.security.AppUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * RBAC evaluátor (MVP).
 *
 * Čte data z {@link AppUserPrincipal} (JWT claims: companyRole, projectRoles, scopes).
 * Podporuje:
 * - Globální scopy (např. "projects:read").
 * - Agregované scopy pro TEAM "write" (TEAM_ADD | TEAM_REMOVE | TEAM_UPDATE_ROLE).
 * - Projektové scopy (Sprint 3) – z projektových rolí přes {@link BuiltInRoles#PROJECT_ROLE_SCOPES}.
 *
 * Bezpečnost:
 * - Defenzivní null-handling (nikdy NPE).
 * - Při neznámém/nezadaném scope vrací false.
 */
@Service
public class RbacServiceImpl implements RbacService {

    public RbacServiceImpl() {
        // DI pro repository přidáme ve Sprintu 3, až budeme ověřovat projektové členství z DB.
    }

    /**
     * True, pokud aktuální uživatel má daný scope (globálně).
     * Podporuje:
     * - přesnou shodu scope (např. "projects:read"),
     * - agregát "team write" (viz TEAM_WRITE_AGGREGATE),
     * - řetězec s oddělovačem '|' (např. "team:add|team:remove").
     */
    @Override
    public boolean hasScope(String scope) {
        if (scope == null || scope.isBlank()) return false;

        var principal = currentPrincipal();
        if (principal == null) return false;
        var scopes = safeScopes(principal);

        // 1) Podpora '|' v podmínce (např. "a|b|c")
        if (scope.indexOf('|') >= 0) {
            return Stream.of(scope.split("\\|"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .anyMatch(scopes::contains);
        }
        // 2) Běžný scope
        return scopes.contains(scope);
    }

    /**
     * True, pokud uživatel má alespoň jeden ze zadaných scope (globálně).
     * Každá položka může být buď konkrétní scope, nebo '|' výraz (viz {@link #hasScope(String)}).
     */
    @Override
    public boolean hasAnyScope(String... scopes) {
        if (scopes == null) return false;
        for (String s : scopes) {
            if (hasScope(s)) return true;
        }
        return false;
    }

    /**
     * True, pokud má uživatel daný scope vzhledem ke konkrétnímu projektu.
     * Logika:
     * - Pokud má globální scope, vrací true.
     * - Jinak (Sprint 3) zkouší z projektové role odvodit scopy pomocí {@link BuiltInRoles#PROJECT_ROLE_SCOPES}.
     */
    @Override
    public boolean hasProjectScope(UUID projectId, String scope) {
        if (projectId == null || scope == null || scope.isBlank()) return false;

        // 1) Globální scope přebíjí projektový
        if (hasScope(scope)) return true;

        var principal = currentPrincipal();
        if (principal == null) return false;

        // 2) Projektové role → scopy (Sprint 3)
        if (principal.getProjectRoles() == null || principal.getProjectRoles().isEmpty()) {
            return false;
        }
        return principal.getProjectRoles().stream()
                .filter(pra -> projectId.equals(pra.projectId()))
                .map(pra -> BuiltInRoles.PROJECT_ROLE_SCOPES.getOrDefault(pra.role(), Collections.emptySet()))
                .anyMatch(roleScopes -> roleScopes.contains(scope) || matchesAggregate(scope, roleScopes));
    }


    /**
     * Zda má uživatel oprávnění číst projekt.
     * Implementováno přes {@link #hasProjectScope(UUID, String)} a globální {@link Scopes#PROJECTS_READ}.
     */
    @Override
    public boolean canReadProject(UUID projectId) {
        return hasProjectScope(projectId, Scopes.PROJECTS_READ);
    }

    /**
     * True, pokud je uživatel (podle JWT) členem projektu (bez kontrol DB).
     * Později doplníme přes repository (Sprint 3).
     */
    @Override
    public boolean isMemberOfProject(UUID projectId) {
        var principal = currentPrincipal();
        if (principal == null || projectId == null) return false;
        return principal.getProjectRoles() != null &&
                principal.getProjectRoles().stream().anyMatch(pra -> projectId.equals(pra.projectId()));
    }

    /**
     * Vrátí aktuální globální scopy z principalu (neměnitelná množina).
     */
    @Override
    public Set<String> currentUserScopes() {
        var principal = currentPrincipal();
        return principal == null ? Collections.emptySet() : safeScopes(principal);
    }


    // ===== pomocné metody =====

    private AppUserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal p)) return null;
        return p;
    }

    private Set<String> safeScopes(AppUserPrincipal p) {
        var s = p.getScopes();
        return (s == null || s.isEmpty()) ? Collections.emptySet() : Collections.unmodifiableSet(s);
    }

    /**
     * Vyhodnotí agregát (např. TEAM_WRITE) vůči dané množině scope.
     */
    private boolean matchesAggregate(String requestedScope, Set<String> haveScopes) {
        if (requestedScope == null || haveScopes == null) return false;
        if (requestedScope.indexOf('|') >= 0) {
            return Stream.of(requestedScope.split("\\|"))
                    .map(String::trim)
                    .anyMatch(haveScopes::contains);
        }
        return false;
    }
}
