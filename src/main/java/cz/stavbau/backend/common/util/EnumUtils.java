// src/main/java/cz/stavbau/backend/common/util/EnumUtils.java
package cz.stavbau.backend.common.util;

import java.util.Locale;

public final class EnumUtils {
    private EnumUtils() {}

    /** Bezpečný parse enumu: trim + upper; vyhazuje IllegalArgumentException s popisem */
    public static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        if (raw == null) throw new IllegalArgumentException("value is null");
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid enum value: " + raw);
        }
    }
}
