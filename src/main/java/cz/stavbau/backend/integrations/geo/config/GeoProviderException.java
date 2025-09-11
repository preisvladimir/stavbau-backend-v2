package cz.stavbau.backend.integrations.geo.config;

public class GeoProviderException extends RuntimeException {
    public GeoProviderException(String message) { super(message); }
    public GeoProviderException(String message, Throwable cause) { super(message, cause); }
}
