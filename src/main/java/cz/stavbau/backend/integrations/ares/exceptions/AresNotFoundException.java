// src/main/java/cz/stavbau/backend/integrations/ares/exceptions/AresNotFoundException.java
package cz.stavbau.backend.integrations.ares.exceptions;

/** 404 – záznam s daným IČO nebyl v ARES nalezen. */
public class AresNotFoundException extends RuntimeException {
    public AresNotFoundException(String message) { super(message); }
    public AresNotFoundException(String message, Throwable cause) { super(message, cause); }
}
