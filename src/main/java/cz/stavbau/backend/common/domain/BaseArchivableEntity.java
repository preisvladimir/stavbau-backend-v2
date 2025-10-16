// src/main/java/cz/stavbau/backend/common/domain/BaseArchivableEntity.java
package cz.stavbau.backend.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Volitelná nadstavba nad BaseEntity pro entity, které lze archivovat.
 * Archivace je nezávislá na soft-delete (deleted).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseArchivableEntity extends BaseEntity {

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "archived_by")
    private UUID archivedBy;

    @Column(name = "archive_reason")
    private String archiveReason;

    public boolean isArchived() {
        return archivedAt != null;
    }

    /** Idempotentní archivace. */
    public void markArchived(UUID by, String reason) {
        if (this.archivedAt == null) {
            this.archivedAt = Instant.now();
            this.archivedBy = by;
            this.archiveReason = reason;
        }
    }

    /** Idempotentní unarchive. */
    public void undoArchive() {
        this.archivedAt = null;
        this.archivedBy = null;
        this.archiveReason = null;
    }
}
