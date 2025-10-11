// src/main/java/cz/stavbau/backend/common/validation/GlobalValidator.java
package cz.stavbau.backend.common.validation;

import cz.stavbau.backend.common.exception.ValidationException;
import cz.stavbau.backend.common.util.EnumUtils;
import cz.stavbau.backend.common.util.TextUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GlobalValidator {

    private final MessageSource messages;

    public GlobalValidator(MessageSource messages) {
        this.messages = messages;
    }

    private String msg(String code) {
        // můžeš nahradit tvým messages.msg(...)
        return messages.getMessage(code, null, code, Locale.getDefault());
    }

    public String requireValidEmail(String raw) {
        String email = TextUtils.normalizeEmail(raw);
        // DTO už může mít @Email – tohle je „obrana v hloubce“
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException(msg("errors.validation.email"));
        }
        return email;
    }

    public <E extends Enum<E>> E requireEnum(Class<E> type, String raw, String errorCode) {
        try {
            return EnumUtils.parseEnum(type, raw);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(msg(errorCode));
        }
    }
}
