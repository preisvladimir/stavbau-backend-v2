package cz.stavbau.backend.team.repo.spec;

import cz.stavbau.backend.team.filter.TeamMemberFilter;
import cz.stavbau.backend.tenants.membership.model.CompanyMember; // tvůj package
import cz.stavbau.backend.users.model.User;        // tvůj package
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class TeamMemberSpecification implements Specification<CompanyMember> {

    private final UUID companyId;
    private final TeamMemberFilter filter;

    @Override
    public Predicate toPredicate(Root<CompanyMember> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> preds = new ArrayList<>();

        // company scope
        preds.add(cb.equal(root.get("companyId"), companyId));

        // bezpečný LEFT JOIN na user – umožní WHERE i ORDER BY na user.*
        Join<CompanyMember, User> user = root.join("user", JoinType.LEFT);

        // fulltext: q kde root je "CompanyMember"
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

        // role
        if (filter.getRole() != null && !filter.getRole().isBlank()) {
            preds.add(cb.equal(root.get("role"), filter.getRole()));
        }

        return cb.and(preds.toArray(new Predicate[0]));
    }
}
