package cz.stavbau.backend.security.jwt;

import org.springframework.http.ResponseCookie;

public final class RefreshCookie {
    private RefreshCookie(){}
    public static final String NAME = "sb_refresh";

    /** Dev: SameSite=Lax, Secure=false (na http://localhost). Prod: Secure=true. */
    public static ResponseCookie of(String token, boolean secure, long maxAgeSeconds) {
        return ResponseCookie.from(NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public static ResponseCookie clearing(boolean secure) {
        return ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
