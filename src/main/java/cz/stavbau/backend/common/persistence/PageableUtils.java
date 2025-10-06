package cz.stavbau.backend.common.persistence;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PageableUtils {
    private static final Logger log = LoggerFactory.getLogger(PageableUtils.class);

    private PageableUtils() {}

    /** Původní jednoduchá verze (kvůli kompatibilitě) */
    public static Pageable from(String sort, int page, int size, String defaultProp) {
        return from(sort, page, size, defaultProp, Set.of(defaultProp), Map.of());
    }

    /**
     * Bezpečný builder Pageable se:
     * - whitelistem povolených property (ochrana před invalidními sloupci)
     * - aliasy (např. "title" -> "name"), s WARN logem
     * - fallbackem na default při neplatném vstupu (s WARN logem)
     */
    public static Pageable from(
            String sort,
            int page,
            int size,
            @NonNull String defaultProp,
            @NonNull Set<String> allowedProps,
            @NonNull Map<String,String> aliases
    ) {
        String prop = defaultProp;
        Sort.Direction dir = Sort.Direction.ASC;

        if (sort != null && !sort.isBlank()) {
            String s = sort.trim();
            String[] parts = s.split(",", 2);
            String rawProp = parts[0].trim();
            String rawDir  = parts.length > 1 ? parts[1].trim() : "asc";

            String resolved = aliases.getOrDefault(rawProp, rawProp);
            if (!resolved.equals(rawProp)) {
                log.warn("Sort alias '{}' -> '{}'", rawProp, resolved);
            }

            if (!allowedProps.contains(resolved)) {
                log.warn("Sort property '{}' není povolena. Fallback na '{}'.", resolved, defaultProp);
                prop = defaultProp;
            } else {
                prop = resolved;
            }

            try {
                dir = Sort.Direction.fromString(rawDir);
            } catch (IllegalArgumentException ex) {
                log.warn("Sort direction '{}' je neplatná. Fallback na 'asc'.", rawDir);
                dir = Sort.Direction.ASC;
            }
        } else {
            // žádný vstup -> default
            prop = defaultProp;
            dir = Sort.Direction.ASC;
        }

        Sort sortObj = Sort.by(new Sort.Order(dir, prop));
        return PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                sortObj
        );
    }

    /** Allow-list pro mapování veřejných názvů sloupců na skutečná pole (často alias do i18n tabulek). */
    public record SortWhitelist(Map<String, String> allowed, Sort defaultSort) {
        public static Builder builder(Sort defaultSort) { return new Builder(defaultSort); }
        public static final class Builder {
            private final Map<String,String> map = new HashMap<>();
            private final Sort defaultSort;
            public Builder(Sort defaultSort) { this.defaultSort = defaultSort; }
            /** e.g. allow("name", "translations.name") */
            public Builder allow(String from, String to) { map.put(from, to); return this; }
            public SortWhitelist build() { return new SortWhitelist(Map.copyOf(map), defaultSort); }
        }
    }

    /** Mapuje `sort=name,asc` přes allow-list, neznámé pole → WARN + default; vždy přidá stabilizační `id DESC`. */
    public static Sort mapAndValidate(String sortParam, SortWhitelist wl, Logger log) {
        if (sortParam == null || sortParam.isBlank()) return wl.defaultSort().and(Sort.by("id").descending());
        var parts = sortParam.split(",", 2);
        var field = parts[0].trim();
        var dir = (parts.length > 1 ? parts[1].trim() : "asc");
        var alias = wl.allowed().get(field);
        if (alias == null) {
            if (log != null) log.warn("Unknown sort '{}', falling back to default", field);
            return wl.defaultSort().and(Sort.by("id").descending());
        }
        var sort = "desc".equalsIgnoreCase(dir) ? Sort.by(alias).descending() : Sort.by(alias).ascending();
        return sort.and(Sort.by("id").descending());
    }
}
