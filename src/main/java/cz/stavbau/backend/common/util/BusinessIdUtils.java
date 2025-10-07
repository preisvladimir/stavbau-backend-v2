// src/main/java/cz/stavbau/backend/common/util/BusinessIdUtils.java
package cz.stavbau.backend.common.util;

/**
 * Utility pro normalizaci identifikátorů firem (IČO apod.).
 * Záměrně bez závislostí; statické metody, čisté transformace.
 */
public final class BusinessIdUtils {

    private BusinessIdUtils() {}

    /**
     * Normalizuje IČO:
     * - trimuje okraje,
     * - odstraní vnitřní whitespace (mezery, taby),
     * - prázdný výsledek vrací jako null (nevyplněné IČO).
     *
     * Příklady:
     *   " 12 34 56 78 " -> "12345678"
     *   "" / "   "     -> null
     *   null           -> null
     */
    public static String normalizeIco(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", "");
        return s.isEmpty() ? null : s;
    }

    // FUTURE:
    // public static boolean isValidIco(String ico) { ... }  // kontrola mod 11 apod.
    // public static String normalizeDic(String raw) { ... } // DIČ (CZ...), atd.
}
