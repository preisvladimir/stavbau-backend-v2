package cz.stavbau.backend.common.exception;

public class BadRequest extends DomainException {
    public BadRequest(String message) { super(message); }
    public BadRequest(String message, Throwable cause) { super(message, cause); }
}
