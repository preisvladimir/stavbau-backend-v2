package cz.stavbau.backend.common.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Základ všech entit: UUID id, audit, soft-delete, optimistic locking.
 * Pozn.: archivace řeš v konkrétních doménách (např. Member.archivedAt),
 * soft-delete je plošný přes deleted/deleted_at/deleted_by.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /* ===== Auditing ===== */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    /* ===== Soft delete ===== */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    /* ===== Optimistic locking ===== */
    @Version
    @Column(name = "version", nullable = false)
    private long version = 0L;

    /* ===== Pomocné metody ===== */

    /** Idempotentní soft-delete. Nastav i deletedBy, pokud ho znáš (např. z SecurityUtils). */
    public void markDeleted(UUID by) {
        if (!this.deleted) {
            this.deleted = true;
            this.deletedAt = Instant.now();
            this.deletedBy = by;
        }
    }

    /** Zrušení soft-delete (většinou nevyužiješ). */
    public void undoDelete() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
