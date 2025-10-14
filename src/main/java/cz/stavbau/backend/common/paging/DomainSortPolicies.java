// src/main/java/cz/stavbau/backend/common/paging/DomainSortPolicies.java
package cz.stavbau.backend.common.paging;

import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

public final class DomainSortPolicies {
    private DomainSortPolicies() {}

    /** Globální strop pro velikost stránky napříč doménami */
    public static final int MAX_PAGE_SIZE = 100;

    /* =========================
     * MEMBERS (Company Members)
     * ========================= */
    public static final int MEMBERS_MAX_PAGE_SIZE = MAX_PAGE_SIZE;

    /** Výchozí řazení: nejnovější nahoře; sekundárně id pro deterministiku */
    public static final Sort MEMBERS_DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.asc("id")
    );

    /**
     * Whitelist povolených sloupců.
     * Pozn.: "user.email" a "user.state" vyžadují LEFT JOIN na user ve Specification.
     */
    public static final Set<String> MEMBERS_ALLOWED_SORT = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "firstName",
            "lastName",
            "phone",
            "role",
            "user.email",
            "user.state"
    );

    /** Aliasování pro pohodlný sort z FE */
    public static final Map<String, String> MEMBERS_SORT_ALIASES = Map.of(
            "email",  "user.email",
            "status", "user.state"   // FE může posílat status → mapujeme na stav uživatele
    );

    /* =====================================
     * TEAM – dočasná kompatibilita (aliasy)
     * ===================================== */
    /** @deprecated Použij MEMBERS_* */
    @Deprecated public static final int  TEAM_MAX_PAGE_SIZE = MEMBERS_MAX_PAGE_SIZE;
    /** @deprecated Použij MEMBERS_* */
    @Deprecated public static final Sort TEAM_DEFAULT_SORT  = MEMBERS_DEFAULT_SORT;
    /** @deprecated Použij MEMBERS_* */
    @Deprecated public static final Set<String> TEAM_ALLOWED_SORT = MEMBERS_ALLOWED_SORT;

    /* =========================
     * CUSTOMERS
     * ========================= */
    public static final int CUSTOMERS_MAX_PAGE_SIZE = MAX_PAGE_SIZE;

    /** Výchozí: jméno abecedně; sekundární id */
    public static final Sort CUSTOMERS_DEFAULT_SORT = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.asc("id")
    );

    public static final Set<String> CUSTOMERS_ALLOWED_SORT = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "name",
            "email",
            "phone",
            "ico",
            "dic"
    );

    /** Aliasy – zkrať názvy/slang z FE → interní property */
    public static final Map<String, String> CUSTOMERS_SORT_ALIASES = Map.of(
            "vat",     "dic",
            "taxId",   "dic",
            "orgId",   "ico"
    );

    /* =========================
     * PROJECTS
     * ========================= */
    public static final int PROJECTS_MAX_PAGE_SIZE = MAX_PAGE_SIZE;

    /** Výchozí: nejnovější projekty; sekundární id */
    public static final Sort PROJECTS_DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.asc("id")
    );

    public static final Set<String> PROJECTS_ALLOWED_SORT = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "code",
            "name",
            "status",
            "type",
            "plannedStartDate",
            "plannedEndDate",
            "customerId",
            "projectManagerId",
            "contractValueNet"
    );

    /** Aliasy – přívětivé klíče pro FE */
    public static final Map<String, String> PROJECTS_SORT_ALIASES = Map.of(
            "pm",         "projectManagerId",
            "start",      "plannedStartDate",
            "end",        "plannedEndDate",
            "value",      "contractValueNet"
    );
}
