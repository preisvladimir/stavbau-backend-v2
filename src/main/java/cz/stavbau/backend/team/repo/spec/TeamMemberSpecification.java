package cz.stavbau.backend.team.repo.spec;

import cz.stavbau.backend.team.filter.TeamMemberFilter;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.users.model.User;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Specifikace pro Team list (company scope + q + role + status).
 * POZOR: upravte ROLE_PROP/STATUS_PROP podle názvů v entitě CompanyMember
 * (např. "role" vs "companyRole", "status" vs "state"…).
 */
@RequiredArgsConstructor
public class TeamMemberSpecification implements Specification<CompanyMember> {

    private final UUID companyId;
    private final TeamMemberFilter filter;

    // Nastavte dle entity:
    private static final String ROLE_PROP = "role";     // nebo "companyRole"
    private static final String STATE_PROP = "state"; // pokud se používá

    @Override
    public Predicate toPredicate(Root<CompanyMember> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> preds = new ArrayList<>();

        // Company scope
        preds.add(cb.equal(root.get("companyId"), companyId));

        // LEFT JOIN na user, aby šlo filtrovat/řadit přes user.*
        Join<CompanyMember, User> user = root.join("user", JoinType.LEFT);

        // Fulltext q
        String q = filter.getQ();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            preds.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(user.get("email")), like)
            ));
        }

        // role (na root – CompanyMember.role)
        if (filter.getRole() != null && !filter.getRole().isBlank()) {
            preds.add(cb.equal(root.get(ROLE_PROP), filter.getRole()));
        }

        // status → pokud máš stav na User (např. state)
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            preds.add(cb.equal(user.get(STATE_PROP), filter.getStatus()));
        }

        return cb.and(preds.toArray(new Predicate[0]));
    }
}
