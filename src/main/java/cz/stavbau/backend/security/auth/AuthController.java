package cz.stavbau.backend.security.auth;

import cz.stavbau.backend.security.jwt.JwtService;
import cz.stavbau.backend.security.jwt.RefreshCookie;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.repo.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private record LoginRequest(@Email String email, @NotBlank String password) {}
    private record AuthResponse(String accessToken, String tokenType) {}
    private record RefreshResponse(String accessToken, String tokenType) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final boolean secureCookies;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                          @Value("${app.security.cookies.secure:false}") boolean secureCookies) {
        this.users = users; this.encoder = encoder; this.jwt = jwt; this.secureCookies = secureCookies;
    }

    /** LOGIN: vydá access + nastaví HttpOnly refresh cookie (rotace jti). */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        User u = users.findByEmail(req.email())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials");
        }
        // nová rotace refresh jti
        UUID newJti = UUID.randomUUID();
        u.setRefreshTokenId(newJti);
        String access = jwt.issueAccessToken(u.getId(), u.getCompanyId(), u.getEmail());
        String refresh = jwt.issueRefreshToken(u.getId(), u.getTokenVersion(), newJti);

        var cookie = RefreshCookie.of(refresh, secureCookies, 14L * 24 * 3600);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(access, "Bearer"));
    }

    /** REFRESH: přečte HttpOnly cookie, ověří verzi a jti, provede ROTACI a vrátí nový access + cookie. */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshCookie) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing_refresh");
        }
        var jws = jwt.parseAndValidate(refreshCookie);
        var c = jws.getBody();
        if (!"refresh".equals(c.getAudience())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_refresh_aud");
        }

        UUID userId = UUID.fromString(c.getSubject());
        int ver = (int) c.get("ver");
        UUID jti = UUID.fromString(c.getId());

        User u = users.findById(userId).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "user_not_found"));

        // Reuse detection + verze
        if (ver != u.getTokenVersion()) {
            // refresh byl vydán před revokací
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh_revoked");
        }
        if (u.getRefreshTokenId() == null || !u.getRefreshTokenId().equals(jti)) {
            // pokus o reuse starého refresh tokenu → okamžitá revokace celé rodiny
            u.setTokenVersion(u.getTokenVersion() + 1);
            u.setRefreshTokenId(null);
            var clear = RefreshCookie.clearing(secureCookies);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clear.toString())
                    .body(Map.of("error", "refresh_reuse_detected"));
        }

        // Rotace: přiděl nový jti, obnov cookie, vrať nový access
        UUID newJti = UUID.randomUUID();
        u.setRefreshTokenId(newJti);

        String access = jwt.issueAccessToken(u.getId(), u.getCompanyId(), u.getEmail());
        String newRefresh = jwt.issueRefreshToken(u.getId(), u.getTokenVersion(), newJti);
        var cookie = RefreshCookie.of(newRefresh, secureCookies, 14L * 24 * 3600);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new RefreshResponse(access, "Bearer"));
    }

    /** LOGOUT: zneplatní aktuální refresh (vymaže jti) a smaže cookie. */
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshCookie) {
        if (refreshCookie != null && !refreshCookie.isBlank()) {
            var jws = jwt.parseAndValidate(refreshCookie);
            UUID userId = UUID.fromString(jws.getBody().getSubject());
            users.findById(userId).ifPresent(u -> u.setRefreshTokenId(null));
        }
        var clear = RefreshCookie.clearing(secureCookies);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clear.toString()).build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Object principal) {
        if (principal instanceof cz.stavbau.backend.security.rbac.AppUserPrincipal p) {
            return ResponseEntity.ok(Map.of(
                    "userId", p.getUserId().toString(),
                    "companyId", p.getCompanyId() != null ? p.getCompanyId().toString() : null,
                    "email", p.getEmail()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error","unauthorized"));
    }

}
