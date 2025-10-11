package cz.stavbau.backend.common.exception;

public class InvalidException extends DomainException {
    public InvalidException(String message) { super(message); }
    public InvalidException(String message, Throwable cause) { super(message, cause); }
}
