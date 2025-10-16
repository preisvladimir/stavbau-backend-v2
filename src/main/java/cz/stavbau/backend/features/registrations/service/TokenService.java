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
}
