package cz.stavbau.backend.common.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * 🔧 Utility třída pro bezpečnou práci se stránkováním a řazením (Sort).
 *
 * <p>
 * Slouží k jednotnému a bezpečnému zpracování {@code sort} parametrů v REST controllerech.
 * Zajišťuje:
 * <ul>
 *   <li>✅ validaci proti allow-listu (ochrana před chybami / injection)</li>
 *   <li>✅ fallback na výchozí řazení při neznámém parametru</li>
 *   <li>✅ jednotné chování napříč moduly (Projects, Customers, Team...)</li>
 * </ul>
 * </p>
 *
 * <p><b>Příklad použití:</b></p>
 * <pre>{@code
 * var allowed = Set.of("createdAt", "code", "translations.name");
 * Sort sort = PageableUtils.safeSortOrDefault(pageable, Sort.by("createdAt").descending(), allowed);
 * var paging = PageRequest.of(page, size, sort);
 * }</pre>
 *
 * <p>Metoda {@link #safeSortOrDefault(Pageable, Sort, Set)} je preferovaná a nahrazuje starší API.</p>
 */
public final class PageableUtils {

    private static final Logger log = LoggerFactory.getLogger(PageableUtils.class);

    private PageableUtils() {
        // utility class – neinstancovat
    }

    // ------------------------------------------------------------------------
    // ✅ Nové API – preferované (všechny controllery by měly používat toto)
    // ------------------------------------------------------------------------

    /**
     * Vrací bezpečný {@link Sort}, který respektuje povolené sloupce a při chybě nebo neznámém sort parametru
     * se vrátí výchozí sort.
     *
     * <p>Pokud {@code pageable == null} nebo {@code pageable.getSort().isUnsorted()}, vrátí se {@code defaultSort}.</p>
     *
     * @param pageable    vstupní {@link Pageable}, např. z controlleru
     * @param defaultSort výchozí sort (např. {@code Sort.by("createdAt").descending()})
     * @param allowed     množina povolených atributů, např. {@code Set.of("createdAt", "code", "translations.name")}
     * @return bezpečný sort bez výjimek
     */
    public static Sort safeSortOrDefault(Pageable pageable, Sort defaultSort, Set<String> allowed) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return defaultSort;
        }

        List<Sort.Order> safeOrders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (allowed.contains(property)) {
                safeOrders.add(order);
            } else {
                log.warn("Ignoring unsupported sort property '{}', allowed={}", property, allowed);
            }
        }

        if (safeOrders.isEmpty()) {
            return defaultSort;
        }

        return Sort.by(safeOrders);
    }

    // ------------------------------------------------------------------------
    // ⚙️ Pomocná struktura – allow-list aliasů (pro i18n tabulky apod.)
    // ------------------------------------------------------------------------

    /**
     * Allow-list pro mapování veřejných názvů sloupců na skutečná pole (např. alias do i18n tabulek).
     * <p>Příklad:</p>
     * <pre>{@code
     * var wl = PageableUtils.SortWhitelist.builder(Sort.by("createdAt").descending())
     *     .allow("name", "translations.name")
     *     .allow("code", "code")
     *     .build();
     * }</pre>
     */
    public record SortWhitelist(Map<String, String> allowed, Sort defaultSort) {
        public static Builder builder(Sort defaultSort) {
            return new Builder(defaultSort);
        }

        public static final class Builder {
            private final Map<String, String> map = new HashMap<>();
            private final Sort defaultSort;

            public Builder(Sort defaultSort) {
                this.defaultSort = defaultSort;
            }

            /** Povolit alias: např. {@code allow("name", "translations.name")} */
            public Builder allow(String from, String to) {
                map.put(from, to);
                return this;
            }

            public SortWhitelist build() {
                return new SortWhitelist(Map.copyOf(map), defaultSort);
            }
        }
    }

    // ------------------------------------------------------------------------
    // 🧱 Původní API (zůstává dočasně kvůli zpětné kompatibilitě)
    // ------------------------------------------------------------------------

    /**
     * @deprecated Použij {@link #safeSortOrDefault(Pageable, Sort, Set)}.
     * <p>Tato metoda bude odstraněna po sjednocení všech controllerů (Q4/2025).</p>
     */
    @Deprecated(forRemoval = true, since = "2025-10")
    public static Sort mapAndValidate(String sortParam, SortWhitelist wl, Logger log) {
        if (sortParam == null || sortParam.isBlank()) {
            return wl.defaultSort().and(Sort.by("id").descending());
        }

        var parts = sortParam.split(",", 2);
        var field = parts[0].trim();
        var dir = (parts.length > 1 ? parts[1].trim() : "asc");
        var alias = wl.allowed().get(field);

        if (alias == null) {
            if (log != null) log.warn("Unknown sort '{}', falling back to default", field);
            return wl.defaultSort().and(Sort.by("id").descending());
        }

        var sort = "desc".equalsIgnoreCase(dir)
                ? Sort.by(alias).descending()
                : Sort.by(alias).ascending();

        return sort.and(Sort.by("id").descending());
    }
}
