package cz.stavbau.backend.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Security utility – bez závislosti na spring-oauth2.
 * Čte companyId/userId/locale primárně z AppUserPrincipal; fallbackem z principal interface nebo z details (Map).
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** Bezpečně vrátí Authentication z kontextu (nebo null). */
    public static Authentication getAuthenticationOrNull() {
        try {
            var ctx = SecurityContextHolder.getContext();
            return (ctx != null) ? ctx.getAuthentication() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Bezpečně vrátí principal z kontextu (nebo null). */
    public static Object getPrincipalOrNull() {
        var auth = getAuthenticationOrNull();
        return (auth != null) ? auth.getPrincipal() : null;
    }

    /* =========================
     * companyId
     * ========================= */
    public static Optional<UUID> currentCompanyId() {
        Object p = getPrincipalOrNull();
        if (p instanceof AppUserPrincipal ap && ap.getCompanyId() != null) {
            return Optional.of(ap.getCompanyId());
        }
        if (p instanceof HasCompanyId withCompany) {
            UUID cid = withCompany.getCompanyId();
            if (cid != null) return Optional.of(cid);
        }
        var auth = getAuthenticationOrNull();
        if (auth != null && auth.getDetails() instanceof Map<?, ?> map) {
            UUID parsed = parseUuid(map.get("companyId"));
            if (parsed != null) return Optional.of(parsed);
        }
        return Optional.empty();
    }

    public static UUID requireCompanyId() {
        return currentCompanyId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("auth.company.required"));
    }

    /* =========================
     * userId  (DOPLNĚNO)
     * ========================= */
    /** Primárně z AppUserPrincipal.getUserId(), případně z details mapy (userId|sub). */
    public static Optional<UUID> currentUserId() {
        Object p = getPrincipalOrNull();
        if (p instanceof AppUserPrincipal ap && ap.getUserId() != null) {
            return Optional.of(ap.getUserId());
        }
        // volitelně: pokud používáš jiné rozhraní
        if (p instanceof HasUserId withUser) {
            UUID uid = withUser.getUserId();
            if (uid != null) return Optional.of(uid);
        }
        var auth = getAuthenticationOrNull();
        if (auth != null && auth.getDetails() instanceof Map<?, ?> map) {
            // podporuj více klíčů – závisí na IdP (Keycloak/JWT "sub", apod.)
            UUID parsed = firstNonNullUuid(
                    parseUuid(map.get("userId")),
                    parseUuid(map.get("sub"))
            );
            if (parsed != null) return Optional.of(parsed);
        }
        return Optional.empty();
    }

    /** Vrátí userId, nebo hodí 401 s kódem auth.user.required */
    public static UUID requireUserId() {
        return currentUserId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("auth.user.required"));
    }

    /* =========================
     * locale
     * ========================= */
    public static Optional<Locale> currentUserLocale() {
        Object p = getPrincipalOrNull();
        if (p instanceof HasPreferredLocale withLocale) {
            Locale loc = withLocale.getPreferredLocale();
            if (loc != null) return Optional.of(loc);
        }
        var auth = getAuthenticationOrNull();
        if (auth != null && auth.getDetails() instanceof Map<?, ?> map) {
            Object raw = map.get("locale");
            if (raw instanceof Locale l) return Optional.of(l);
            if (raw instanceof String s && !s.isBlank()) {
                try { return Optional.of(Locale.forLanguageTag(s)); } catch (Exception ignored) {}
            }
        }
        return Optional.empty();
    }

    /* =========================
     * helpers & optional interfaces
     * ========================= */
    private static UUID parseUuid(Object raw) {
        if (raw instanceof UUID uuid) return uuid;
        if (raw instanceof String s && !s.isBlank()) {
            try { return UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    @SafeVarargs
    private static <T> T firstNonNullUuid(T... vals) {
        for (T v : vals) if (v != null) return v;
        return null;
    }

    /** Volitelná rozhraní pro vlastní principal (pokud chcete mít čisté API bez Map details). */
    public interface HasCompanyId { UUID getCompanyId(); }
    public interface HasPreferredLocale { Locale getPreferredLocale(); }
    /** (DOPLNĚNO) */
    public interface HasUserId { UUID getUserId(); }
}
