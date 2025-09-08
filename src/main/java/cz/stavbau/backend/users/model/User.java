package cz.stavbau.backend.users.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "locale", length = 10)
    private String locale;

    /** Rotovatelná revokace celé „rodiny“ refresh tokenů. */
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    /** Aktuálně platný refresh token (jti). */
    @Column(name = "refresh_token_id")
    private UUID refreshTokenId;

    // ---- Getters & Setters (explicitně) ----
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public int getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(int tokenVersion) { this.tokenVersion = tokenVersion; }

    public UUID getRefreshTokenId() { return refreshTokenId; }
    public void setRefreshTokenId(UUID refreshTokenId) { this.refreshTokenId = refreshTokenId; }
}
