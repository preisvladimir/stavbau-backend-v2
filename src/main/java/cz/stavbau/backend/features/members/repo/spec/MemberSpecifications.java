package cz.stavbau.backend.features.members.repo.spec;

import cz.stavbau.backend.features.members.model.Member;
import cz.stavbau.backend.identity.users.model.User;
import cz.stavbau.backend.identity.users.model.UserState;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Stream;

public final class MemberSpecifications {

    private MemberSpecifications() {}

    private static final String ATTR_COMPANY_ID = "companyId";
    private static final String ATTR_DELETED    = "deleted";
    private static final String ATTR_FIRST_NAME = "firstName";
    private static final String ATTR_LAST_NAME  = "lastName";
    private static final String ATTR_PHONE      = "phone";
    private static final String ATTR_ROLE       = "role";

    private static final String REL_USER = "user";
    private static final String U_EMAIL  = "email";
    private static final String U_STATE  = "state";

    private static Join<Member, User> userJoin(Root<Member> root) {
        for (Join<Member, ?> j : root.getJoins()) {
            if (REL_USER.equals(j.getAttribute().getName())) {
                @SuppressWarnings("unchecked") Join<Member, User> uj = (Join<Member, User>) j;
                return uj;
            }
        }
        return root.join(REL_USER, JoinType.LEFT);
    }

    public static Specification<Member> byCompany(UUID companyId) {
        return (root, cq, cb) -> cb.equal(root.get(ATTR_COMPANY_ID), companyId);
    }

    public static Specification<Member> notDeleted() {
        return (root, cq, cb) -> cb.isFalse(root.get(ATTR_DELETED));
    }

    /** Fulltext přes Member.first/last/phone + User.email (tokenizace, case-insensitive) */
    public static Specification<Member> text(String q) {
        if (q == null || q.isBlank()) return null;

        List<String> tokens = Stream.of(q.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(s -> !s.isBlank()).toList();
        if (tokens.isEmpty()) return null;

        return (root, cq, cb) -> {
            Join<Member, User> user = userJoin(root);
            // ⚠️ DISTINCT NE – dělá problém s ORDER BY user.email v PG
            List<Predicate> tokenPreds = new ArrayList<>(tokens.size());
            for (String t : tokens) {
                String like = "%" + t + "%";
                tokenPreds.add(cb.or(
                        cb.like(cb.lower(root.get(ATTR_FIRST_NAME)), like),
                        cb.like(cb.lower(root.get(ATTR_LAST_NAME)),  like),
                        cb.like(cb.lower(root.get(ATTR_PHONE)),      like),
                        cb.like(cb.lower(user.get(U_EMAIL)),         like)
                ));
            }
            return cb.and(tokenPreds.toArray(Predicate[]::new));
        };
    }

    public static Specification<Member> byRole(String role) {
        CompanyRoleName roleEnum = parseEnum(role, CompanyRoleName.class);
        if (roleEnum == null) return null;
        return (root, cq, cb) -> cb.equal(root.get(ATTR_ROLE), roleEnum);
    }

    public static Specification<Member> byStatus(String status) {
        UserState st = parseEnum(status, UserState.class);
        if (st == null) return null;
        return (root, cq, cb) -> cb.equal(userJoin(root).get(U_STATE), st);
    }

    public static Specification<Member> hasIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, cq, cb) -> root.get("id").in(ids);
    }

    private static <E extends Enum<E>> E parseEnum(String value, Class<E> type) {
        if (value == null || value.isBlank()) return null;
        String up = value.trim().toUpperCase(Locale.ROOT);
        try { return Enum.valueOf(type, up); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
