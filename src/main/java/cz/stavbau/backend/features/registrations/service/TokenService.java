// src/main/java/cz/stavbau/backend/features/registrations/service/TokenService.java
package cz.stavbau.backend.features.registrations.service;

import java.time.Instant;

public interface TokenService {
    final class GeneratedToken {
        public final String rawToken;
        public final String tokenHash;
        public final Instant expiresAt;
        public GeneratedToken(String rawToken, String tokenHash, Instant expiresAt) {
            this.rawToken = rawToken; this.tokenHash = tokenHash; this.expiresAt = expiresAt;
        }
    }
    GeneratedToken issueVerificationToken();

    /** Deterministický HMAC-SHA256 hash z raw tokenu – shodný algoritmus jako při issue. */
    String hash(String rawToken);
}
