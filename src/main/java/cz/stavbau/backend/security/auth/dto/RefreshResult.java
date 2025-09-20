package cz.stavbau.backend.security.auth.dto;

public record RefreshResult(RefreshResponse body, String cookieValue) {}
