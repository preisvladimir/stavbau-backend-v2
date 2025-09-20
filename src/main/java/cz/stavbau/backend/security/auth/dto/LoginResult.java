package cz.stavbau.backend.security.auth.dto;

public record LoginResult(AuthResponse body, String cookieValue) {}
