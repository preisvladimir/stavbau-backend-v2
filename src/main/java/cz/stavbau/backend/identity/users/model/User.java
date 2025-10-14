package cz.stavbau.backend.identity.users.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    // ---- Getters & Setters (explicitně) ----
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

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private UserState state = UserState.ACTIVE;

    @Column(name = "password_needs_reset", nullable = false)
    private boolean passwordNeedsReset = false;

    @Column(name = "invited_at")
    private OffsetDateTime invitedAt;

}
