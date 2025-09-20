package cz.stavbau.backend.security.auth;

import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.security.auth.dto.AuthResponse;
import cz.stavbau.backend.security.auth.dto.RefreshResponse;
import cz.stavbau.backend.security.auth.dto.LoginResult;
import cz.stavbau.backend.security.auth.dto.MeResponse;
import cz.stavbau.backend.security.auth.dto.RefreshResult;
import cz.stavbau.backend.security.jwt.JwtService;
import cz.stavbau.backend.security.jwt.RefreshCookie;
import cz.stavbau.backend.security.rbac.BuiltInRoles;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AuthService: obsahuje business logiku pro login/refresh/logout a sestavení /me response.
 * - Bez závislostí na web vrstvě (jen vrací hodnoty, které controller použije).
 * - RBAC claims jsou řízené přes flag v JwtService (isRbacClaimsEnabled).
 * - Login/Refresh jsou fail-safe: při chybě RBAC větve padáme na legacy token (bez claims).
 */
@Service
public class AuthService {

    private final UserRepository users;
    private final CompanyMemberRepository companyMembers;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    private final boolean secureCookies;
    private final int refreshMaxAgeSeconds;

    public AuthService(UserRepository users,
                       CompanyMemberRepository companyMembers,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       @Value("${app.security.cookies.secure:false}") boolean secureCookies,
                       @Value("${app.security.refresh.maxAgeSeconds:1209600}") int refreshMaxAgeSeconds // 14 dní default
    ) {
        this.users = users;
        this.companyMembers = companyMembers;
        this.encoder = encoder;
        this.jwt = jwt;
        this.secureCookies = secureCookies;
        this.refreshMaxAgeSeconds = refreshMaxAgeSeconds;
    }

    /**
     * Ověří přihlašovací údaje, provede rotaci refresh JTI, vydá access+refresh.
     * Pokud je RBAC zapnuté, pokusí se přidat companyRole+scopes do access tokenu,
     * při chybě claims větve tiše spadne na legacy access token (bez RBAC).
     */
    @Transactional
    public LoginResult login(String email, String password) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("auth.invalid_credentials"));

        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new BadCredentialsException("auth.invalid_credentials");
        }

        // Rotace refresh JTI
        UUID newJti = UUID.randomUUID();
        u.setRefreshTokenId(newJti);

        String access = buildAccessTokenFailSafe(u);
        int tokenVersion = u.getTokenVersion(); // primitivum int – nikdy null
        String refresh = jwt.issueRefreshToken(u.getId(), tokenVersion, newJti);

        var cookie = RefreshCookie.of(refresh, secureCookies, refreshMaxAgeSeconds);
        return new LoginResult(new AuthResponse(access, "Bearer"), cookie.toString());
    }

    /**
     * Ověří refresh cookie, reuse detekci a verzi; provede rotaci JTI a vydá nový access+refresh.
     * RBAC claims jsou aplikovány jako ve loginu (fail-safe).
     */
    @Transactional
    public RefreshResult refresh(String refreshCookie) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing_refresh");
        }

        var jws = jwt.parseAndValidate(refreshCookie);
        var c = jws.getBody();
        if (!"refresh".equals(c.getAudience())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_refresh_aud");
        }

        UUID userId = UUID.fromString(c.getSubject());
        int ver = (int) c.get("ver");
        UUID jti = UUID.fromString(c.getId());

        User u = users.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user_not_found"));

        // Reuse detekce + kontrola verze
        if (ver != u.getTokenVersion()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh_revoked");
        }
        if (u.getRefreshTokenId() == null || !u.getRefreshTokenId().equals(jti)) {
            // reuse -> revokace celé rodiny
            u.setTokenVersion(u.getTokenVersion() + 1);
            u.setRefreshTokenId(null);
            var clear = RefreshCookie.clearing(secureCookies);
            // Caller (controller) by měl vrátit 401 a Set-Cookie s clear; tady jen vyhodíme 401
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh_reuse_detected");
        }

        // Rotace refresh JTI
        UUID newJti = UUID.randomUUID();
        u.setRefreshTokenId(newJti);

        String access = buildAccessTokenFailSafe(u);
        String newRefresh = jwt.issueRefreshToken(u.getId(), u.getTokenVersion(), newJti);
        var cookie = RefreshCookie.of(newRefresh, secureCookies, refreshMaxAgeSeconds);
        return new RefreshResult(new RefreshResponse(access, "Bearer"), cookie.toString());
    }

    /**
     * Zneplatní aktuální refresh (vymaže JTI) a vrátí header pro smazání cookie.
     * Idempotentní – nevadí, když cookie není.
     */
    @Transactional
    public String logout(String refreshCookie) {
        if (refreshCookie != null && !refreshCookie.isBlank()) {
            var jws = jwt.parseAndValidate(refreshCookie);
            UUID userId = UUID.fromString(jws.getBody().getSubject());
            users.findById(userId).ifPresent(u -> u.setRefreshTokenId(null));
        }
        var clear = RefreshCookie.clearing(secureCookies);
        return clear.toString();
    }

    /** Sestaví /auth/me odpověď z principalu. */
    public MeResponse buildMeResponse(AppUserPrincipal p) {
        var resp = new MeResponse();
        resp.id = p.getUserId();
        resp.companyId = p.getCompanyId();
        resp.email = p.getEmail();
        resp.companyRole = p.getCompanyRole();
        resp.projectRoles = p.getProjectRoles();
        resp.scopes = p.getScopes();
        return resp;
    }

    // ===== Interní pomocné metody =====

    /**
     * Vydá access token. Pokud je RBAC ON, přidá companyRole+scopes s fail-safe fallbackem na legacy token.
     */
    private String buildAccessTokenFailSafe(User u) {
        if (!jwt.isRbacClaimsEnabled()) {
            return jwt.issueAccessToken(u.getId(), u.getCompanyId(), u.getEmail());
        }
        try {
            UUID userId = u.getId();
            UUID companyId = u.getCompanyId(); // může být null

            CompanyRoleName role = resolveCompanyRoleSafe(userId, companyId);
            Set<String> scopes = BuiltInRoles.COMPANY_ROLE_SCOPES.getOrDefault(role, Set.of());

            return jwt.issueAccessToken(
                    userId,
                    companyId,
                    u.getEmail(),
                    role,
                    List.of(), // projectRoles (Sprint 3)
                    scopes
            );
        } catch (Exception ex) {
            // nikdy nespadnout v login/refresh kvůli RBAC
            // (můžeš nahradit loggerem)
            System.err.println("[WARN] RBAC claims emission failed, falling back to legacy token: " + ex.getMessage());
            return jwt.issueAccessToken(u.getId(), u.getCompanyId(), u.getEmail());
        }
    }

    /**
     * Načte roli uživatele ve firmě s defenzivními fallbacky.
     */
    private CompanyRoleName resolveCompanyRoleSafe(UUID userId, UUID companyId) {
        if (companyId == null) return CompanyRoleName.VIEWER;
        try {
            return companyMembers.findByUserIdAndCompanyId(userId, companyId)
                    .map(CompanyMember::getRole)
                    .orElse(CompanyRoleName.VIEWER);
        } catch (Exception e) {
            return CompanyRoleName.VIEWER;
        }
    }
}
