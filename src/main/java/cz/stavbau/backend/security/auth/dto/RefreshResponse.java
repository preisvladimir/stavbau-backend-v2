package cz.stavbau.backend.security.auth.dto;

/** Tělo odpovědi pro /auth/refresh. */
public record RefreshResponse(String accessToken, String tokenType) {}
