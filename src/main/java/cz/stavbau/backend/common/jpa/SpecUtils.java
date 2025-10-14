package cz.stavbau.backend.common.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.util.Arrays;
import java.util.Locale;

public final class SpecUtils {
    private SpecUtils() {}

    public static Predicate ilike(CriteriaBuilder cb, Expression<String> expr, String term) {
        var like = "%" + term.trim().toLowerCase(Locale.ROOT) + "%";
        return cb.like(cb.lower(expr), like);
    }

    public static String[] tokenize(String q, int maxTerms) {
        if (q == null || q.isBlank()) return new String[0];
        return Arrays.stream(q.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .limit(Math.max(1, maxTerms))
                .toArray(String[]::new);
    }

}
