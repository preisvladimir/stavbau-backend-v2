// src/main/java/cz/stavbau/backend/common/paging/DomainSortPolicies.java
package cz.stavbau.backend.common.paging;

import org.springframework.data.domain.Sort;
import java.util.Set;

public final class DomainSortPolicies {
    private DomainSortPolicies() {}
    public static final int MAX_PAGE_SIZE = 100;

    // --- TEAM ---
    public static final int  TEAM_MAX_PAGE_SIZE = MAX_PAGE_SIZE;
    public static final Sort TEAM_DEFAULT_SORT  = Sort.by("user.email").ascending();
    public static final Set<String> TEAM_ALLOWED_SORT = Set.of(
            "id", "firstName", "lastName", "phone",
            "user.email", "user.state", "user.role",
            "createdAt", "updatedAt"
    );

    // --- CUSTOMERS ---
    public static final int CUSTOMER_MAX_PAGE_SIZE = MAX_PAGE_SIZE;
    public static final Sort CUSTOMER_DEFAULT_SORT = Sort.by("name").ascending();
    public static final Set<String> CUSTOMER_ALLOWED_SORT = Set.of(
            "id","name","email","phone","ico","dic","createdAt","updatedAt"
    );

    // --- PROJECTS ---
    public static final int PROJECT_MAX_PAGE_SIZE = MAX_PAGE_SIZE;
    public static final Sort PROJECT_DEFAULT_SORT = Sort.by("createdAt").descending();
    public static final Set<String> PROJECT_ALLOWED_SORT = Set.of(
            "createdAt","updatedAt","code","name","status","type",
            "plannedStartDate","plannedEndDate","customerId","projectManagerId","contractValueNet"
    );
}
