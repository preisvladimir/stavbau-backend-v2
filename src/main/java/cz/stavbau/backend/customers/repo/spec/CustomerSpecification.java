package cz.stavbau.backend.customers.repo.spec;

import cz.stavbau.backend.customers.filter.CustomerFilter;
import cz.stavbau.backend.customers.model.Customer;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Company scope + fulltext + (volitelné) type/status. */
@RequiredArgsConstructor
public class CustomerSpecification implements Specification<Customer> {

    private final UUID companyId;
    private final CustomerFilter filter;

    // Uprav názvy podle entity (pokud máš jiné):
    private static final String TYPE_PROP = "type";
    private static final String STATUS_PROP = "status";

    @Override
    public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> preds = new ArrayList<>();

        // tenancy guard
        preds.add(cb.equal(root.get("companyId"), companyId));

        // q: name, email, phone, ico, dic
        String q = filter.getQ();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            preds.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(root.get("ico")), like),
                    cb.like(cb.lower(root.get("dic")), like)
            ));
        }

        // type
        String type = filter.getType();
        if (type != null && !type.isBlank()) {
            preds.add(cb.equal(cb.upper(root.get(TYPE_PROP)), type));
        }

        // status
        String status = filter.getStatus();
        if (status != null && !status.isBlank()) {
            preds.add(cb.equal(cb.upper(root.get(STATUS_PROP)), status));
        }

        return cb.and(preds.toArray(new Predicate[0]));
    }
}
