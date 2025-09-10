// src/main/java/cz/stavbau/backend/integrations/ares/exceptions/AresUnavailableException.java
package cz.stavbau.backend.integrations.ares.exceptions;

/** 503 – ARES nedostupný (timeout, 5xx, síť, apod.). */
public class AresUnavailableException extends RuntimeException {
    public AresUnavailableException(String message) { super(message); }
    public AresUnavailableException(String message, Throwable cause) { super(message, cause); }
}
