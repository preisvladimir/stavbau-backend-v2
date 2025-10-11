package cz.stavbau.backend.projects.persistence;

import cz.stavbau.backend.projects.filter.ProjectFilter;
import cz.stavbau.backend.projects.model.Project;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class ProjectSpecification implements Specification<Project> {
    private final UUID companyId;
    private final ProjectFilter f;

    @Override
    public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> q, CriteriaBuilder cb) {
        List<Predicate> P = new ArrayList<>();

        // --- Tenant guard ---
        P.add(cb.equal(root.get("companyId"), companyId));

        // --- Helpers pro normalizaci výrazu na sloupcích ---
        // unaccent_immutable(lower(column)) – aby se trefil výrazový GIN index s gin_trgm_ops
        Expression<String> normName = unaccentLower(cb, root.get("name"));
        Expression<String> normCode = cb.lower(root.get("code")); // obvykle bez diakritiky
        // Pokud chceš i code diakritika-insensitive, nahraď na:
        // Expression<String> normCode = unaccentLower(cb, root.get("code"));

        // --- Fulltext q → name, code ---
        if (hasText(f.getQ())) {
            String likeParam = "%" + f.getQ().trim().toLowerCase(Locale.ROOT) + "%";

            // Vzor normalizujeme stejně jako sloupec, aby LIKE odpovídal indexu i diakritice
            Expression<String> likeNameParam = unaccentLower(cb, cb.literal(likeParam));
            Expression<String> likeCodeParam = cb.lower(cb.literal(likeParam));
            // Pokud jsi dal unaccent i na code, použij:
            // Expression<String> likeCodeParam = unaccentLower(cb, cb.literal(likeParam));

            P.add(cb.or(
                    cb.like(normName, likeNameParam),
                    cb.like(normCode, likeCodeParam)
            ));
        }

        // --- Jednoduché filtry ---
        if (hasText(f.getCode())) {
            String codeLike = "%" + f.getCode().trim().toLowerCase(Locale.ROOT) + "%";
            P.add(cb.like(normCode, cb.lower(cb.literal(codeLike))));
            // Pokud máš unaccent na code:
            // P.add(cb.like(normCode, unaccentLower(cb, cb.literal(codeLike))));
        }

        if (f.getStatus() != null) P.add(cb.equal(root.get("status"), f.getStatus()));
        if (f.getType() != null)   P.add(cb.equal(root.get("type"), f.getType()));

        if (f.getCustomerId() != null)       P.add(cb.equal(root.get("customerId"), f.getCustomerId()));
        if (f.getProjectManagerId() != null) P.add(cb.equal(root.get("projectManagerId"), f.getProjectManagerId()));

        // active: true → pouze nearchivované, false → pouze archivované
        if (f.getActive() != null) {
            if (Boolean.TRUE.equals(f.getActive())) P.add(cb.isNull(root.get("archivedAt")));
            else                                    P.add(cb.isNotNull(root.get("archivedAt")));
        }

        // --- Date ranges ---
        if (f.getPlannedStartFrom() != null) P.add(cb.greaterThanOrEqualTo(root.get("plannedStartDate"), f.getPlannedStartFrom()));
        if (f.getPlannedStartTo()   != null) P.add(cb.lessThanOrEqualTo(root.get("plannedStartDate"),   f.getPlannedStartTo()));
        if (f.getPlannedEndFrom()   != null) P.add(cb.greaterThanOrEqualTo(root.get("plannedEndDate"),   f.getPlannedEndFrom()));
        if (f.getPlannedEndTo()     != null) P.add(cb.lessThanOrEqualTo(root.get("plannedEndDate"),     f.getPlannedEndTo()));

        // --- Value ranges ---
        if (f.getMinContractValueNet() != null) {
            P.add(cb.greaterThanOrEqualTo(root.get("contractValueNet"), f.getMinContractValueNet()));
        }
        if (f.getMaxContractValueNet() != null) {
            P.add(cb.lessThanOrEqualTo(root.get("contractValueNet"), f.getMaxContractValueNet()));
        }

        return cb.and(P.toArray(new Predicate[0]));
    }

    /** unaccent_immutable(lower(expr)) */
    private Expression<String> unaccentLower(CriteriaBuilder cb, Expression<String> expr) {
        return cb.function("public.unaccent_immutable", String.class, cb.lower(expr));
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
