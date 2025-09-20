package cz.stavbau.backend.security.auth;

import cz.stavbau.backend.security.AppUserPrincipal;
import cz.stavbau.backend.security.auth.dto.AuthResponse;
import cz.stavbau.backend.security.auth.dto.LoginRequest;
import cz.stavbau.backend.security.auth.dto.MeResponse;
import cz.stavbau.backend.security.jwt.JwtService;
import cz.stavbau.backend.security.jwt.RefreshCookie;
import cz.stavbau.backend.security.rbac.BuiltInRoles;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.repo.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private record RefreshResponse(String accessToken, String tokenType) {}

    private final AuthService auth;
    private final UserRepository users;
    private final CompanyMemberRepository companyMemberRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final boolean secureCookies;

    public AuthController(AuthService auth, UserRepository users, CompanyMemberRepository companyMemberRepository, PasswordEncoder encoder, JwtService jwt,
                          @Value("${app.security.cookies.secure:false}") boolean secureCookies) {
        this.auth = auth;
        this.users = users;
        this.companyMemberRepository = companyMemberRepository;
        this.encoder = encoder; this.jwt = jwt; this.secureCookies = secureCookies;
    }
    /** LOGIN: vydá access + nastaví HttpOnly refresh cookie (rotace jti). */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        User u = users.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials");
        }

        // rotace refresh jti
        UUID newJti = UUID.randomUUID();
        u.setRefreshTokenId(newJti);

        String access;
        if (jwt.isRbacClaimsEnabled()) {
            try {
                // POZOR na pořadí: userId, companyId
                UUID userId = u.getId();
                UUID companyId = u.getCompanyId(); // může být null

                // role – pokud repo vrátí null nebo selže, padneme na VIEWER
                CompanyRoleName role = companyMemberRepository
                        .findByUserIdAndCompanyId(userId, companyId)
                        .map(CompanyMember::getRole)
                        .orElse(CompanyRoleName.VIEWER);

                // scopes – bezpečný default na prázdnou sadu (nikdy null)
                java.util.Set<String> scopes =
                        java.util.Objects.requireNonNullElse(
                                cz.stavbau.backend.security.rbac.BuiltInRoles.COMPANY_ROLE_SCOPES.get(role),
                                java.util.Set.of()
                        );

                // RBAC overload – projectRoles zatím prázdné
                access = jwt.issueAccessToken(
                        userId,
                        companyId,
                        u.getEmail(),
                        role,
                        java.util.List.of(),
                        scopes
                );
            } catch (Exception ex) {
                // cokoli v RBAC větvi selže → spadni na legacy a logni varování
                // (případně použij tvůj logger)
                System.err.println("[WARN] RBAC claims emission failed, falling back to legacy token: " + ex.getMessage());
                access = jwt.issueAccessToken(u.getId(), u.getCompanyId(), u.getEmail());
            }
        } else {
            // legacy větev beze změny
            access = jwt.issueAccessToken(u.getId(), u.getCompanyId(), u.getEmail());
        }

        // Map.of neakceptuje null → pojistka na tokenVersion
        int tokenVersion = u.getTokenVersion();
        String refresh = jwt.issueRefreshToken(u.getId(), tokenVersion, newJti);

        var cookie = RefreshCookie.of(refresh, secureCookies, 14L * 24 * 3600);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(access, "Bearer"));
    }
    /** LOGIN: vydá access + nastaví HttpOnly refresh cookie (rotace jti). */
    @PostMapping("/loginnew")
    public ResponseEntity<AuthResponse> loginnew(@RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(auth.login(req.email(), req.password()));
        } catch (BadCredentialsException e) {
            // Tady by měl tvůj ApiExceptionHandler vrátit 401 s i18n "auth.invalid_credentials"
            throw e;
        }
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

    @GetMapping("/mex")
    public ResponseEntity<?> me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Object principal) {
        if (principal instanceof AppUserPrincipal p) {
            return ResponseEntity.ok(Map.of(
                    "userId", p.getUserId().toString(),
                    "companyId", p.getCompanyId() != null ? p.getCompanyId().toString() : null,
                    "email", p.getEmail(),
                    "scopes", p.getScopes(),
                    "projectRoles",p.getProjectRoles(),
                    "companyRole", p.getCompanyRole()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error","unauthorized"));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        var p = (AppUserPrincipal) authentication.getPrincipal();
        var resp = new MeResponse();
        resp.id = p.getUserId();
        resp.companyId = p.getCompanyId();
        resp.email = p.getEmail();
        resp.companyRole = p.getCompanyRole();
        resp.projectRoles = p.getProjectRoles();
        resp.scopes = p.getScopes();
        return ResponseEntity.ok(resp);
    }
}
