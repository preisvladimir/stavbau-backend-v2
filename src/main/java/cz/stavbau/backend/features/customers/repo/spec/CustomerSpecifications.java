// src/main/java/cz/stavbau/backend/features/customers/repo/spec/CustomerSpecifications.java
package cz.stavbau.backend.features.customers.repo.spec;

import cz.stavbau.backend.features.customers.model.Customer;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Stream;

public final class CustomerSpecifications {

    private CustomerSpecifications() {}

    // --- názvy atributů na jednom místě
    private static final String ATTR_COMPANY_ID = "companyId";
    private static final String ATTR_DELETED    = "deleted";
    private static final String ATTR_NAME       = "name";
    private static final String ATTR_EMAIL      = "email";
    private static final String ATTR_PHONE      = "phone";
    private static final String ATTR_ICO        = "ico";
    private static final String ATTR_DIC        = "dic";
    private static final String ATTR_TYPE       = "type";
    private static final String ATTR_STATUS     = "status";

    /** company scope */
    public static Specification<Customer> byCompany(UUID companyId) {
        return (root, cq, cb) -> cb.equal(root.get(ATTR_COMPANY_ID), companyId);
    }

    /** soft-delete guard; pokud používáš CommonSpecifications.notDeleted(), klidně nepoužívej tuto metodu */
    public static Specification<Customer> notDeleted() {
        return (root, cq, cb) -> cb.isFalse(root.get(ATTR_DELETED));
    }

    /** Fulltext přes name/email/phone/ico/dic; tokenizace, case-insensitive; všechny tokeny musí projít (AND) */
    public static Specification<Customer> text(String q) {
        if (q == null || q.isBlank()) return null;

        List<String> tokens = Stream.of(q.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
        if (tokens.isEmpty()) return null;

        return (root, cq, cb) -> andAll(tokens.stream().map(t -> likeAny(root, cb, t)).toList(), cb);
    }

    /** type = case-insensitive match (string/enum name) */
    public static Specification<Customer> byType(String type) {
        if (isBlank(type)) return null;
        String up = type.trim().toUpperCase(Locale.ROOT);
        return (root, cq, cb) -> cb.equal(cb.upper(root.get(ATTR_TYPE)), up);
    }

    /** status = case-insensitive match (string/enum name) */
    public static Specification<Customer> byStatus(String status) {
        if (isBlank(status)) return null;
        String up = status.trim().toUpperCase(Locale.ROOT);
        return (root, cq, cb) -> cb.equal(cb.upper(root.get(ATTR_STATUS)), up);
    }

    /** filtr dle sady ID */
    public static Specification<Customer> hasIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, cq, cb) -> root.get("id").in(ids);
    }

    // ---- helpers ----

    private static Predicate likeAny(Root<Customer> root, CriteriaBuilder cb, String token) {
        String like = "%" + token + "%";
        return cb.or(
                cb.like(cb.lower(root.get(ATTR_NAME)),  like),
                cb.like(cb.lower(root.get(ATTR_EMAIL)), like),
                cb.like(cb.lower(root.get(ATTR_PHONE)), like),
                cb.like(cb.lower(root.get(ATTR_ICO)),   like),
                cb.like(cb.lower(root.get(ATTR_DIC)),   like)
        );
    }

    private static Predicate andAll(List<Predicate> preds, CriteriaBuilder cb) {
        return preds.size() == 1 ? preds.get(0) : cb.and(preds.toArray(Predicate[]::new));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
