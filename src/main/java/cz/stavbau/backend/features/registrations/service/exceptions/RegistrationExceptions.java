// src/main/java/cz/stavbau/backend/features/registrations/service/exceptions/RegistrationExceptions.java
package cz.stavbau.backend.features.registrations.service.exceptions;

public final class RegistrationExceptions {
    private RegistrationExceptions() {}

    public static class NotFound extends RuntimeException {
        public NotFound(String code) { super(code); }
    }
    public static class Validation extends RuntimeException {
        public Validation(String code) { super(code); }
    }
    public static class RateLimited extends RuntimeException {
        public RateLimited() { super("rateLimit.exceeded"); }
    }
    public static class TokenInvalidOrExpired extends RuntimeException {
        public TokenInvalidOrExpired() { super("token.invalidOrExpired"); }
    }
    public static class AlreadyConfirmed extends RuntimeException {
        public AlreadyConfirmed() { super("registration.alreadyConfirmed"); }
    }
    public static class Conflict extends RuntimeException {
        public Conflict(String code) { super(code); }
    }
}
