// src/main/java/cz/stavbau/backend/features/projects/persistence/ProjectSpecifications.java
package cz.stavbau.backend.features.projects.persistence;

import cz.stavbau.backend.features.projects.model.Project;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public final class ProjectSpecifications {

    private ProjectSpecifications() {}

    // --- názvy atributů centralizovaně ---
    private static final String ATTR_COMPANY_ID       = "companyId";
    private static final String ATTR_DELETED          = "deleted";
    private static final String ATTR_ARCHIVED_AT      = "archivedAt";
    private static final String ATTR_NAME             = "name";
    private static final String ATTR_CODE             = "code";
    private static final String ATTR_STATUS           = "status";
    private static final String ATTR_TYPE             = "type";
    private static final String ATTR_CUSTOMER_ID      = "customerId";
    private static final String ATTR_PM_ID            = "projectManagerId";
    private static final String ATTR_PLANNED_START    = "plannedStartDate";
    private static final String ATTR_PLANNED_END      = "plannedEndDate";
    private static final String ATTR_CONTRACT_VALUE   = "contractValueNet";

    // --- základní stavebnice ---
    public static Specification<Project> byCompany(UUID companyId) {
        return (root, cq, cb) -> cb.equal(root.get(ATTR_COMPANY_ID), companyId);
    }

    public static Specification<Project> notDeleted() {
        return (root, cq, cb) -> cb.isFalse(root.get(ATTR_DELETED));
    }

    /** fulltext přes name + code; tokenizace; diakritika-insensitive přes unaccent_immutable(lower(...)) */
    public static Specification<Project> text(String q) {
        if (isBlank(q)) return null;
        List<String> tokens = tokensOf(q);
        if (tokens.isEmpty()) return null;

        return (root, cq, cb) -> {
            Expression<String> normName = unaccentLower(cb, root.get(ATTR_NAME));
            Expression<String> normCode = cb.lower(root.get(ATTR_CODE)); // code obvykle bez diakritiky
            // Chceš-li i code bez diakritiky, použij unaccentLower pro code:
            // Expression<String> normCode = unaccentLower(cb, root.get(ATTR_CODE));

            List<Predicate> perToken = new ArrayList<>(tokens.size());
            for (String t : tokens) {
                String like = "%" + t + "%";
                Expression<String> likeName = unaccentLower(cb, cb.literal(like));
                Expression<String> likeCode = cb.lower(cb.literal(like));
                // nebo s unaccent: Expression<String> likeCode = unaccentLower(cb, cb.literal(like));
                perToken.add(cb.or(
                        cb.like(normName, likeName),
                        cb.like(normCode, likeCode)
                ));
            }
            return andAll(perToken, cb);
        };
    }

    public static Specification<Project> byCodeLike(String code) {
        if (isBlank(code)) return null;
        return (root, cq, cb) ->
                cb.like(cb.lower(root.get(ATTR_CODE)), "%" + code.trim().toLowerCase(Locale.ROOT) + "%");
    }

    public static <E extends Enum<E>> Specification<Project> byStatus(E status) {
        if (status == null) return null;
        return (root, cq, cb) -> cb.equal(root.get(ATTR_STATUS), status);
    }

    public static <E extends Enum<E>> Specification<Project> byType(E type) {
        if (type == null) return null;
        return (root, cq, cb) -> cb.equal(root.get(ATTR_TYPE), type);
    }

    public static Specification<Project> byCustomerId(UUID customerId) {
        if (customerId == null) return null;
        return (root, cq, cb) -> cb.equal(root.get(ATTR_CUSTOMER_ID), customerId);
    }

    public static Specification<Project> byProjectManagerId(UUID pmId) {
        if (pmId == null) return null;
        return (root, cq, cb) -> cb.equal(root.get(ATTR_PM_ID), pmId);
    }

    /** active=true => nearchivované; false => archivované; null => bez filtru */
    public static Specification<Project> byActive(Boolean active) {
        if (active == null) return null;
        return (root, cq, cb) -> Boolean.TRUE.equals(active)
                ? cb.isNull(root.get(ATTR_ARCHIVED_AT))
                : cb.isNotNull(root.get(ATTR_ARCHIVED_AT));
    }

    public static Specification<Project> plannedStartBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        return (root, cq, cb) -> betweenDates(root, cb, ATTR_PLANNED_START, from, to);
    }

    public static Specification<Project> plannedEndBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        return (root, cq, cb) -> betweenDates(root, cb, ATTR_PLANNED_END, from, to);
    }

    public static Specification<Project> contractValueBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        return (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>(2);
            if (min != null) p.add(cb.greaterThanOrEqualTo(root.get(ATTR_CONTRACT_VALUE), min));
            if (max != null) p.add(cb.lessThanOrEqualTo(root.get(ATTR_CONTRACT_VALUE),  max));
            return andAll(p, cb);
        };
    }

    public static Specification<Project> hasIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, cq, cb) -> root.get("id").in(ids);
    }

    // --- helpers ---
    private static Expression<String> unaccentLower(CriteriaBuilder cb, Expression<String> expr) {
        // očekává Postgres funkci public.unaccent_immutable(text)
        return cb.function("public.unaccent_immutable", String.class, cb.lower(expr));
    }

    private static Predicate andAll(List<Predicate> preds, CriteriaBuilder cb) {
        if (preds == null || preds.isEmpty()) return cb.conjunction();
        return preds.size() == 1 ? preds.get(0) : cb.and(preds.toArray(Predicate[]::new));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static List<String> tokensOf(String q) {
        return Stream.of(q.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(t -> !t.isBlank())
                .toList();
    }

    private static Predicate betweenDates(Root<Project> root, CriteriaBuilder cb, String attr,
                                          LocalDate from, LocalDate to) {
        List<Predicate> p = new ArrayList<>(2);
        if (from != null) p.add(cb.greaterThanOrEqualTo(root.get(attr), from));
        if (to   != null) p.add(cb.lessThanOrEqualTo(root.get(attr),   to));
        return andAll(p, cb);
    }
}
