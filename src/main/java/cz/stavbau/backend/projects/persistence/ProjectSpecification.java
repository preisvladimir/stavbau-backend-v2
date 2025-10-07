package cz.stavbau.backend.projects.persistence;

import cz.stavbau.backend.projects.filter.ProjectFilter;
import cz.stavbau.backend.projects.model.Project;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class ProjectSpecification implements Specification<Project> {

    private final UUID companyId;
    private final ProjectFilter filter;
    private final Locale locale; // ✅ přidáno – preferovaný jazyk pro překlady (MVP)

    @Override
    public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        // ✅ kvůli LEFT JOIN (translations) a možnému ORDER BY nad překlady
        //    zabráníme duplicitám
        cq.distinct(true);

        List<Predicate> and = new ArrayList<>();

        // Company scope
        and.add(cb.equal(root.get("companyId"), companyId));

        // ✅ JOIN na překlady názvů (kolekce "translations" s polem "name" a "language")
        //    Pokud se entita/atribut jmenuje jinak, uprav názvy níže.
        //Join<Object, Object> tr = root.join("translations", JoinType.LEFT);

        // Jednoduché filtry (beze změn)
        if (notBlank(filter.getCode())) {
            and.add(ilike(cb, root.get("code"), filter.getCode()));
        }
        if (filter.getStatus() != null) {
            and.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getCustomerId() != null) {
            and.add(cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getProjectManagerId() != null) {
            and.add(cb.equal(root.get("projectManagerId"), filter.getProjectManagerId()));
        }

        // Aktivita/archiv
        if (filter.getActive() != null) {
            if (filter.getActive()) {
                and.add(cb.isNull(root.get("archivedAt")));
            } else {
                and.add(cb.isNotNull(root.get("archivedAt")));
            }
        }

        // Rozsahy plánovaných dat
        range(and, cb, root.get("plannedStartDate"), filter.getPlannedStartFrom(), filter.getPlannedStartTo());
        range(and, cb, root.get("plannedEndDate"),   filter.getPlannedEndFrom(),   filter.getPlannedEndTo());

        // ✅ Preferovaný jazyk pro překlad (MVP tolerantní: akceptujeme i NULL/odlišný jazyk)
        //String lang = (locale != null ? locale.getLanguage() : "cs");
        //and.add(cb.or(
        //        cb.isNull(tr.get("language")),
        //        cb.equal(cb.lower(tr.get("language")), lang.toLowerCase(Locale.ROOT))
        //));
        // ✅ Oprava názvu pole: používáme 'locale' (např. 'cs' nebo 'cs-CZ') místo 'language'
        //    Pro MVP zatím NEFILTRUJEME dle locale, jen držíme LEFT JOIN kvůli sortu "name".
        //    (Chceme vracet všechny projekty bez ohledu na existenci/locale překladu.)
        //    Pokud chcete jemně preferovat aktuální locale později, přidáme prefix-match:
        //    cb.like(cb.lower(tr.get("locale")), (locale != null ? locale.getLanguage().toLowerCase() : "cs") + "%")


        // Fulltext q → tokeny AND; uvnitř OR rozšířeno o translations.name
        for (String term : tokenize(filter.getQ(), 6)) {
            String like = "%" + term.toLowerCase(Locale.ROOT) + "%";
            Predicate or = cb.or(
                    cb.like(cb.lower(root.get("code")), like)
                   // cb.like(cb.lower(tr.get("name")), like) // ✅ přidáno
            );
            and.add(or);
        }

        return cb.and(and.toArray(new Predicate[0]));
    }

    private static void range(List<Predicate> and, CriteriaBuilder cb, Path<LocalDate> path, LocalDate from, LocalDate to) {
        if (from != null) and.add(cb.greaterThanOrEqualTo(path, from));
        if (to != null)   and.add(cb.lessThanOrEqualTo(path, to));
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
