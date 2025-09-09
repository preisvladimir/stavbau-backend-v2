package cz.stavbau.backend.security.rbac;

import java.util.Set;
import java.util.UUID;

/** Rozhraní pro vyhodnocování přístupů (RBAC 2.1 §7.2). */
public interface RbacService {

    /** Má aktuální uživatel daný globální scope? (Company-level) */
    boolean hasScope(String scope);

    /** Má aktuální uživatel alespoň jeden ze zadaných scope? */
    boolean hasAnyScope(String... scopes);

    /** Má aktuální uživatel scope v rámci projektu #projectId? */
    boolean hasProjectScope(UUID projectId, String scope);

    /** Může číst projekt (typicky read-only guard)? */
    boolean canReadProject(UUID projectId);

    /** Je členem projektu? (member check) */
    boolean isMemberOfProject(UUID projectId);

    /** Agregované scopes aktuálního uživatele (JWT / SecurityContext). */
    Set<String> currentUserScopes();
}
