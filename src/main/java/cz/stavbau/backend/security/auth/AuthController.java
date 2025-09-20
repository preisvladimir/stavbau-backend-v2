package cz.stavbau.backend.security.auth;

import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.security.auth.dto.AuthResponse;
import cz.stavbau.backend.security.auth.dto.LoginRequest;
import cz.stavbau.backend.security.auth.dto.MeResponse;
import cz.stavbau.backend.security.jwt.RefreshCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller pro autentizační tok (login/refresh/logout) a získání identity (/me).
 * <p>
 * Controller je záměrně „tenký“: deleguje veškerou business logiku do {@link AuthService}
 * a pouze skládá HTTP odpovědi (status + hlavičky + těla).
 */
@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /**
     * Přihlášení uživatele.
     * <ul>
     *   <li>Ověří přihlašovací údaje,</li>
     *   <li>provede rotaci refresh JTI,</li>
     *   <li>vrátí access token v těle a nastaví HttpOnly refresh cookie.</li>
     * </ul>
     *
     * @param req vstupní DTO s {@code email} a {@code password}
     * @return 200 OK + {@link AuthResponse} + hlavička {@code Set-Cookie} s refresh tokenem
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        var result = auth.login(req.email(), req.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookieValue())
                .body(result.body());
    }

    /**
     * Obnovení (refresh) access tokenu pomocí HttpOnly refresh cookie.
     * <ul>
     *   <li>Ověří platnost refresh tokenu, verzi a reuse,</li>
     *   <li>provede rotaci JTI,</li>
     *   <li>vrátí nový access token a přenastaví refresh cookie.</li>
     * </ul>
     *
     * @param refreshCookie hodnota refresh cookie (HttpOnly)
     * @return 200 OK + {@code { "accessToken": "...", "tokenType": "Bearer" }} + nová {@code Set-Cookie}
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshCookie) {
        var result = auth.refresh(refreshCookie);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookieValue())
                .body(result.body());
    }

    /**
     * Odhlášení uživatele.
     * <ul>
     *   <li>Zneplatní aktuální refresh (vymaže JTI v DB),</li>
     *   <li>pošle clearing cookie prohlížeči.</li>
     * </ul>
     *
     * @param refreshCookie hodnota refresh cookie (může chybět – endpoint je idempotentní)
     * @return 204 No Content + clearing {@code Set-Cookie}
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshCookie) {
        var cookieValue = auth.logout(refreshCookie);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieValue)
                .build();
    }

    /**
     * Vrátí kontext přihlášeného uživatele pro FE (toggly, RBAC atd.).
     *
     * @param authentication Spring Security {@link Authentication} s {@link AppUserPrincipal}
     * @return 200 OK + {@link MeResponse} ({@code id}, {@code companyId}, {@code email}, {@code companyRole}, {@code scopes}, {@code projectRoles})
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        var p = (AppUserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(auth.buildMeResponse(p));
    }
}
