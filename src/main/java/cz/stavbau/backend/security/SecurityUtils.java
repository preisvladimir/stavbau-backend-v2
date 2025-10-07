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
 * Čte companyId/locale primárně z AppUserPrincipal; fallbackem z principal interface nebo z details (Map).
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

    /**
     * Aktuální companyId:
     * 0) AppUserPrincipal.getCompanyId()
     * 1) Principal implementuje HasCompanyId
     * 2) Authentication.getDetails() je Map a obsahuje "companyId" (UUID nebo String)
     */
    public static Optional<UUID> currentCompanyId() {
        // 0) primárně z AppUserPrincipal
        Object p = getPrincipalOrNull();
        if (p instanceof AppUserPrincipal ap && ap.getCompanyId() != null) {
            return Optional.of(ap.getCompanyId());
        }

        // 1) případně z interface na vlastním principalu
        if (p instanceof HasCompanyId withCompany) {
            UUID cid = withCompany.getCompanyId();
            if (cid != null) return Optional.of(cid);
        }

        // 2) fallback z details (Map)
        var auth = getAuthenticationOrNull();
        if (auth != null && auth.getDetails() instanceof Map<?, ?> map) {
            Object raw = map.get("companyId");
            if (raw instanceof UUID uuid) return Optional.of(uuid);
            if (raw instanceof String s && !s.isBlank()) {
                try { return Optional.of(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
            }
        }

        return Optional.empty();
    }
    /**
      * Vrátí aktuální companyId, nebo vyhodí {@link AuthenticationCredentialsNotFoundException}
      * s kódem "auth.company.required". Slouží k odstranění duplicitní logiky v service třídách.
      */
    public static UUID requireCompanyId() {
        return currentCompanyId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("auth.company.required"));
    }

    /**
     * Preferované uživatelské locale:
     * 0) Principal implementuje HasPreferredLocale (pokud existuje)
     * 1) Authentication.getDetails() je Map a obsahuje "locale" (Locale nebo String "cs-CZ"/"en")
     */
    public static Optional<Locale> currentUserLocale() {
        // 0) případně z interface na vlastním principalu (AppUserPrincipal zatím locale nemá)
        Object p = getPrincipalOrNull();
        if (p instanceof HasPreferredLocale withLocale) {
            Locale loc = withLocale.getPreferredLocale();
            if (loc != null) return Optional.of(loc);
        }

        // 1) fallback z details (Map)
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

    /** Volitelná rozhraní pro vlastní principal (pokud chcete mít čisté API bez Map details). */
    public interface HasCompanyId {
        UUID getCompanyId();
    }
    public interface HasPreferredLocale {
        Locale getPreferredLocale();
    }
}
