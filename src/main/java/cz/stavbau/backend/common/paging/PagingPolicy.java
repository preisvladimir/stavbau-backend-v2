package cz.stavbau.backend.common.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

/** Jednotná politika pro page/size/sort. */
public final class PagingPolicy {
    private PagingPolicy() {}

    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    /** Clamp page na >= 0 (null => 0). */
    public static int clampPage(Integer page) {
        return page == null ? 0 : Math.max(0, page);
    }

    public static int clampPage(int page) {
        return Math.max(0, page);
    }

    /** Cap size na <= max (null/<=0 => 20; max<=0 => DEFAULT_MAX_PAGE_SIZE). */
    public static int capSize(Integer size, int max) {
        int s = (size == null || size <= 0) ? 20 : size;
        int m = (max <= 0) ? DEFAULT_MAX_PAGE_SIZE : max;
        return Math.min(s, m);
    }

    public static int capSize(int size, int max) {
        return Math.max(1, Math.min(size, max));
    }

    /** Bezpečné řazení: povolí jen property z allow-list; jinak defaultSort. */
    public static Sort safeSortOrDefault(Sort requested, Sort defaultSort, Set<String> allowlist) {
        if (requested == null || requested.isUnsorted()) return defaultSort;
        if (allowlist == null || allowlist.isEmpty()) return defaultSort;

        List<Sort.Order> keep = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Sort.Order o : requested) {
            String prop = o.getProperty();
            if (prop == null) continue;
            if (!allowlist.contains(prop)) continue;
            if (seen.add(prop)) keep.add(o); // dedup property
        }
        return keep.isEmpty() ? defaultSort : Sort.by(keep);
    }

    /** Z Pageable → bezpečný PageRequest (clamp, cap, allow-list sort). */
    //public static PageRequest ensure(Pageable pageable, int maxSize, Sort defaultSort, Set<String> allowlist) {
      //  int page = clampPage(pageable != null ? pageable.getPageNumber() : 0);
       // int size = capSize(pageable != null ? pageable.getPageSize() : null, maxSize);
       // Sort safe = safeSortOrDefault(pageable != null ? pageable.getSort() : null, defaultSort, allowlist);
       // return PageRequest.of(page, size, safe);
    //}

    /** Varianta z „raw“ parametrů. */
    public static PageRequest of(Integer page, Integer size, Sort requestedSort, int maxSize, Sort defaultSort, Set<String> allowlist) {
        int p = clampPage(page);
        int s = capSize(size, maxSize);
        Sort safe = safeSortOrDefault(requestedSort, defaultSort, allowlist);
        return PageRequest.of(p, s, safe);
    }

    public static PageRequest ensure(
            Pageable pageable,
            int maxSize,
            Sort defaultSort,
            Set<String> allowedProps
    ) {
        return ensureWithAliases(pageable, maxSize, defaultSort, allowedProps, Map.of());
    }

    public static PageRequest ensureWithAliases(
            Pageable pageable,
            int maxSize,
            Sort defaultSort,
            Set<String> allowedProps,
            Map<String, String> aliases // např. "email" -> "user.email"
    ) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), maxSize);
        int page = Math.max(pageable.getPageNumber(), 0);

        // 1) Aliasování
        Sort aliased = applyAliases(pageable.getSort(), aliases);

        // 2) Whitelist
        Sort safe = whitelist(aliased, allowedProps)
                .orElse(defaultSort);

        return PageRequest.of(page, size, safe);
    }

    /** Nahradí property podle alias mapy (pokud existuje) */
    public static Sort applyAliases(Sort sort, Map<String, String> aliases) {
        if (sort == null || sort.isUnsorted() || aliases == null || aliases.isEmpty()) {
            return sort == null ? Sort.unsorted() : sort;
        }
        List<Sort.Order> mapped = new ArrayList<>();
        for (Sort.Order o : sort) {
            String prop = o.getProperty();
            String mappedProp = aliases.getOrDefault(prop, prop);
            mapped.add(new Sort.Order(o.getDirection(), mappedProp)
                    .ignoreCase()
                    .nullsLast()); // volitelné
        }
        return Sort.by(mapped);
    }

    /** Vrátí whitelisted sort nebo prázdné, když nic neprošlo */
    public static Optional<Sort> whitelist(Sort sort, Set<String> allowedProps) {
        if (sort == null || sort.isUnsorted()) return Optional.empty();
        if (allowedProps == null || allowedProps.isEmpty()) return Optional.of(Sort.unsorted());

        List<Sort.Order> safe = new ArrayList<>();
        for (Sort.Order o : sort) {
            if (allowedProps.contains(o.getProperty())) {
                safe.add(o);
            }
        }
        return safe.isEmpty() ? Optional.empty() : Optional.of(Sort.by(safe));
    }
}
