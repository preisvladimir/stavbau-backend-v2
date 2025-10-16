package cz.stavbau.backend.features.registrations.service.impl;

import cz.stavbau.backend.features.registrations.config.RegistrationsProperties;
import cz.stavbau.backend.features.registrations.service.TokenService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

public class TokenServiceImpl implements TokenService {

    private final SecureRandom random;
    private final byte[] hmacSecret;
    private final Duration ttl;
    private final Clock clock;

    public TokenServiceImpl(RegistrationsProperties props, Clock clock) {
        this.random = new SecureRandom();
        this.clock = clock == null ? Clock.systemUTC() : clock;
        String secretRef = props.getToken().getSecretRef();
        String secret = System.getenv(secretRef);
        if (secret == null || secret.isBlank()) {
            // Pro local/dev: dovolíme fallback na "dev-secret", ať se dá spustit bez ENV
            secret = "DEV_ONLY_DoNotUseInProd";
        }
        this.hmacSecret = secret.getBytes();
        this.ttl = Duration.parse(props.getToken().getTtl());
    }

    @Override
    public GeneratedToken issueVerificationToken() {
        byte[] raw = new byte[32]; // 256-bit
        random.nextBytes(raw);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String tokenHash = hmacSha256(rawToken);
        Instant expiresAt = Instant.now(clock).plus(ttl);
        return new GeneratedToken(rawToken, tokenHash, expiresAt);
    }

    private String hmacSha256(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
            byte[] out = mac.doFinal(input.getBytes());
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 failed", e);
        }
    }

    @Override
    public String hash(String rawToken) {
        return hmacSha256(rawToken);
    }
}
