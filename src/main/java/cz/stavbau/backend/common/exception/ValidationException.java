// src/main/java/cz/stavbau/backend/common/exception/ValidationException.java
package cz.stavbau.backend.common.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 400 Bad Request – validační/uživatelská chyba.
 * Lze přidat field-errory (např. "email" → "invalid").
 *
 * Konvence:
 * - message = i18n key (např. "errors.validation.email")
 * - violations[].code = i18n key pro konkrétní pole (např. "invalid", "required")
 */
public class ValidationException extends DomainException {

    private final List<Violation> violations;

    /** Jednoduchá validační chyba bez field-errů. */
    public ValidationException(String message) {
        super(message);
        this.violations = Collections.emptyList();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.violations = Collections.emptyList();
    }

    /** Validační chyba s polem porušení. */
    public ValidationException(String message, List<Violation> violations) {
        super(message);
        this.violations = (violations == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(violations)));
    }

    /** Přístup k nemodifikovatelnému seznamu porušení. */
    public List<Violation> getViolations() {
        return violations;
    }

    /** Jedna položka porušení – konkrétní pole + kód + (volitelně) čitelná zpráva. */
    public static final class Violation {
        private final String field;   // např. "email"
        private final String code;    // např. "invalid", "required"
        private final String message; // volitelné, může být i18n key nebo přímo text

        public Violation(String field, String code) {
            this(field, code, null);
        }

        public Violation(String field, String code, String message) {
            this.field = Objects.requireNonNull(field, "field");
            this.code = Objects.requireNonNull(code, "code");
            this.message = message;
        }

        public String getField() { return field; }
        public String getCode() { return code; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "Violation{field='" + field + "', code='" + code + "', message='" + message + "'}";
        }
    }

    // --- Pohodlné továrny ---

    /** Jedno pole: field="email", code="invalid" (message = i18n key celé chyby). */
    public static ValidationException ofField(String globalMessageKey, String field, String code) {
        return new ValidationException(globalMessageKey,
                List.of(new Violation(field, code)));
    }

    /** Více polí (message = i18n key celé chyby). */
    public static ValidationException ofFields(String globalMessageKey, List<Violation> violations) {
        return new ValidationException(globalMessageKey, violations);
    }
}
