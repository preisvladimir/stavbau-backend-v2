// src/main/java/cz/stavbau/backend/common/jpa/CommonSpecifications.java
package cz.stavbau.backend.common.jpa;

import cz.stavbau.backend.common.domain.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Sdílené specifikace pro soft-delete a archivaci.
 * Pozn.: notArchived()/archived() používej jen na entity, které mají sloupec "archivedAt"
 * (tj. dědí z BaseArchivableEntity). Pro ostatní je NEVOLAT.
 */
public final class CommonSpecifications {
    private CommonSpecifications(){}

    /** Vynutí DISTINCT bez dalšího filtru. */
    public static <T> Specification<T> distinct() {
        return (root, cq, cb) -> {
            cq.distinct(true);
            return cb.conjunction();
        };
    }

    /** Soft-delete guard: pouze ne-smazané. */
    public static <T extends BaseEntity> Specification<T> notDeleted() {
        return (root, cq, cb) -> cb.isFalse(root.get("deleted"));
    }

    /** Archivace guard: pouze NEarchivované (archived_at IS NULL). */
    public static <T> Specification<T> notArchived() {
        return (root, cq, cb) -> cb.isNull(root.get("archivedAt"));
    }

    /** Archivované (archived_at IS NOT NULL). */
    public static <T> Specification<T> archived() {
        return (root, cq, cb) -> cb.isNotNull(root.get("archivedAt"));
    }
}
