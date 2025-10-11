// src/main/java/cz/stavbau/backend/common/filter/Filters.java
package cz.stavbau.backend.common.filter;

import cz.stavbau.backend.common.util.TextUtils;
import org.springframework.util.StringUtils;

import java.util.function.Function;

public final class Filters {
    private Filters() {}

    public static String normQ(String q) { return TextUtils.normalizeBlankToNull(q); }

    public static String normUpper(String s) {
        String v = TextUtils.normalizeBlankToNull(s);
        return v == null ? null : v.toUpperCase();
    }

    public static String normEnum(String v) {
        return StringUtils.hasText(v) ? v.trim().toUpperCase() : null;
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String clean(String s) {
        if (!StringUtils.hasText(s)) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String upper(String s) {
        if (!StringUtils.hasText(s)) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }





    public static <T> T orDefault(T v, T def) { return v != null ? v : def; }

    public static <T> T mapOrNull(String raw, Function<String, T> mapper) {
        String norm = TextUtils.normalizeBlankToNull(raw);
        return norm == null ? null : mapper.apply(norm);
    }
}
