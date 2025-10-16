package cz.stavbau.backend.common.jpa;

import cz.stavbau.backend.common.domain.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

public final class CommonSpecifications {
    private CommonSpecifications(){}

    public static <T> Specification<T> distinct() {
        return (root, cq, cb) -> {
            cq.distinct(true);
            return cb.conjunction(); // žádný filtr, jen DISTINCT
        };
    }

    public static <T> Specification<T> notDeleted() {
        return (root, cq, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static <T> Specification<T> archivedOnly() {
        return (root, cq, cb) -> cb.isNotNull(root.get("archivedAt"));
    }

    public static <T> Specification<T> notArchived() {
        return (root, cq, cb) -> cb.isNull(root.get("archivedAt"));
    }

}