package cz.stavbau.backend.invoices.repo.spec;

import cz.stavbau.backend.invoices.model.Customer;
import cz.stavbau.backend.invoices.filter.CustomerFilter;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class CustomerSpecification implements Specification<Customer> {

    private final UUID companyId;
    private final CustomerFilter filter;

    @Override
    public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        List<Predicate> and = new ArrayList<>();

        // Company scope
        and.add(cb.equal(root.get("companyId"), companyId));

        // Jednoduché filtry (contains, case-insensitive)
        if (notBlank(filter.getName()))  and.add(ilike(cb, root.get("name"),  filter.getName()));
        if (notBlank(filter.getIco()))   and.add(ilike(cb, root.get("ico"),   filter.getIco()));
        if (notBlank(filter.getDic()))   and.add(ilike(cb, root.get("dic"),   filter.getDic()));
        if (notBlank(filter.getEmail())) and.add(ilike(cb, root.get("email"), filter.getEmail()));

        // Fulltext q: každé slovo musí padnout aspoň do jednoho OR pole (AND mezi slovy)
        for (String term : tokenize(filter.getQ(), 6)) {
            String like = "%" + term.toLowerCase(Locale.ROOT) + "%";
            Predicate or = cb.or(
                    cb.like(cb.lower(root.get("name")),  like),
                    cb.like(cb.lower(root.get("ico")),   like),
                    cb.like(cb.lower(root.get("dic")),   like),
                    cb.like(cb.lower(root.get("email")), like)
            );
            and.add(or);
        }

        // Aktivita/archiv (pokud entita má boolean "active" nebo podobný sloupec)
        if (filter.getActive() != null) {
            and.add(cb.equal(root.get("active"), filter.getActive()));
        }

        return cb.and(and.toArray(new Predicate[0]));
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private static Predicate ilike(CriteriaBuilder cb, Path<String> path, String term) {
        String like = "%" + term.trim().toLowerCase(Locale.ROOT) + "%";
        return cb.like(cb.lower(path), like);
    }

    private static String[] tokenize(String q, int max) {
        if (q == null || q.isBlank()) return new String[0];
        return java.util.Arrays.stream(q.trim().split("\\s+"))
                .filter(w -> !w.isBlank())
                .limit(Math.max(1, max))
                .toArray(String[]::new);
    }
}
