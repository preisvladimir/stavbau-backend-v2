package cz.stavbau.backend.common.exception;

public class ConflictException extends DomainException {
    public ConflictException(String message) { super(message); }
    public ConflictException(String message, Throwable cause) { super(message, cause); }
}
