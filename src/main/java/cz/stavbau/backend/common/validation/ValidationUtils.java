// cz.stavbau.backend.common.validation.ValidationUtils
package cz.stavbau.backend.common.validation;

import cz.stavbau.backend.common.exception.ValidationException;
import java.time.LocalDate;
import java.util.Locale;

public final class ValidationUtils {
    private ValidationUtils() {}

    public static void validateDates(LocalDate start, LocalDate end, String i18nKey) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException(i18nKey != null ? i18nKey : "errors.dates.invalid_range");
        }
    }

    public static void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("errors.dates.invalid_range");
        }
    }

    public static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    public static String blankToNull(String s) {
        if (s == null) return null; String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
