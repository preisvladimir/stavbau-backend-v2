package cz.stavbau.backend.features.registrations.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "registration_cases")
public class RegistrationCase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // Stavová logika
    @Column(name = "status", nullable = false, length = 32)
    private String status; // ENUM uložený jako STRING (EMAIL_SENT, ...)

    @Column(name = "next_action", nullable = false, length = 32)
    private String nextAction; // ENUM uložený jako STRING (VERIFY_EMAIL|NONE)

    // Kontakt
    @Column(name = "email", nullable = false, columnDefinition = "citext")
    private String email;

    // Token
    @Column(name = "token_hash", length = 255)
    private String tokenHash;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    // Anti-spam / resend
    @Column(name = "cooldown_until")
    private Instant cooldownUntil;

    // Idempotence
    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    // Draft & consents (JSONB) - v PR1 jako plain JSON String, konvertor přidáme později
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "company_draft", columnDefinition = "jsonb", nullable = false)
    private String companyDraft;

    // consents
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consents", columnDefinition = "jsonb", nullable = false)
    private String consents;

    // Meta

    @Column(name = "requested_ip", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String requestedIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "locale", nullable = false, length = 16)
    private String locale;

    // ares_lookup_meta
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ares_lookup_meta", columnDefinition = "jsonb")
    private String aresLookupMeta;

    // Výsledek po potvrzení
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    // Životní cyklus
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Audit & version
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

}
