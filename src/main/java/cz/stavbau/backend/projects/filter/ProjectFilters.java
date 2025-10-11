// src/main/java/cz/stavbau/backend/projects/filter/ProjectFilters.java
package cz.stavbau.backend.projects.filter;

import cz.stavbau.backend.common.filter.Filters;

import java.time.LocalDate;

/**
 * Jediný zdroj pravdy pro normalizaci filtrů „Projects“.
 * - trim/empty→null pro Stringy
 * - q: normalizace na čistý dotaz (bez multi-logiky; tokenizace řeší Specification)
 * - validace/opravná rotace rozsahů dat (from/to)
 */
public final class ProjectFilters {

    private ProjectFilters() {}

    public static ProjectFilter normalize(ProjectFilter in) {
        ProjectFilter f = (in == null) ? new ProjectFilter() : in;

        ProjectFilter out = new ProjectFilter();
        // 1) stringy
        out.setQ(Filters.normQ(f.getQ()));               // trim, empty→null (case řeší DB lower() ve spec)
        out.setCode(Filters.trimToNull(f.getCode()));    // přesné/contains dle spec

        // 2) enum/UUID/boolean – bez změny (enum už Spring binduje)
        out.setStatus(f.getStatus());
        out.setCustomerId(f.getCustomerId());
        out.setProjectManagerId(f.getProjectManagerId());
        out.setActive(f.getActive());

        // 3) rozsahy dat – ošetři pořadí hranic (swap, pokud uživatel zadal opačně)
        LocalDate psFrom = f.getPlannedStartFrom();
        LocalDate psTo   = f.getPlannedStartTo();
        if (psFrom != null && psTo != null && psFrom.isAfter(psTo)) {
            LocalDate tmp = psFrom; psFrom = psTo; psTo = tmp;
        }
        out.setPlannedStartFrom(psFrom);
        out.setPlannedStartTo(psTo);

        LocalDate peFrom = f.getPlannedEndFrom();
        LocalDate peTo   = f.getPlannedEndTo();
        if (peFrom != null && peTo != null && peFrom.isAfter(peTo)) {
            LocalDate tmp = peFrom; peFrom = peTo; peTo = tmp;
        }
        out.setPlannedEndFrom(peFrom);
        out.setPlannedEndTo(peTo);

        return out;
    }
}
