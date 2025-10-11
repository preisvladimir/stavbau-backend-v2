package cz.stavbau.backend.common.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    public static PageRequest ensure(Pageable pageable, int maxSize, Sort defaultSort, Set<String> allowlist) {
        int page = clampPage(pageable != null ? pageable.getPageNumber() : 0);
        int size = capSize(pageable != null ? pageable.getPageSize() : null, maxSize);
        Sort safe = safeSortOrDefault(pageable != null ? pageable.getSort() : null, defaultSort, allowlist);
        return PageRequest.of(page, size, safe);
    }

    /** Varianta z „raw“ parametrů. */
    public static PageRequest of(Integer page, Integer size, Sort requestedSort, int maxSize, Sort defaultSort, Set<String> allowlist) {
        int p = clampPage(page);
        int s = capSize(size, maxSize);
        Sort safe = safeSortOrDefault(requestedSort, defaultSort, allowlist);
        return PageRequest.of(p, s, safe);
    }
}
