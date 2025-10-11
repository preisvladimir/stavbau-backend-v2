// src/main/java/cz/stavbau/backend/common/util/TextUtils.java
package cz.stavbau.backend.common.util;

import java.util.Locale;

public final class TextUtils {
    private TextUtils() {}

    /** trim; prázdný → null */
    public static String normalizeBlankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** trim + lower (pro e-maily, loginy apod.) */
    public static String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    /** null-safe „null→0“ pro Long */
    public static long nz(Long v) { return v == null ? 0L : v; }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
