package cz.stavbau.backend.common.api;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

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
}
