package cz.stavbau.backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.issuer:stavbau}") String issuer,
            @Value("${app.security.jwt.accessTokenTtlMinutes:30}") long accessTtlMinutes,
            @Value("${app.security.jwt.refreshTokenTtlDays:14}") long refreshTtlDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlMinutes * 60;
        this.refreshTtlSeconds = refreshTtlDays * 24 * 60 * 60;
    }

    public String issueAccessToken(UUID userId, UUID companyId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .setAudience("api")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .addClaims(Map.of(
                        "cid", companyId != null ? companyId.toString() : null,
                        "email", email
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Vytvoří refresh JWT s jti a tokenVersion – pro rotaci a revokaci. */
    public String issueRefreshToken(UUID userId, int tokenVersion, UUID refreshJti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .setAudience("refresh")
                .setId(refreshJti.toString())                  // jti
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .addClaims(Map.of("ver", tokenVersion))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) throws JwtException {
        return Jwts.parserBuilder().requireIssuer(issuer).setSigningKey(key).build().parseClaimsJws(token);
    }
}
