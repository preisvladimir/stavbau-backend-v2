package cz.stavbau.backend.projects.filter;

import cz.stavbau.backend.projects.model.ProjectStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
public class ProjectFilter {
    /** Fulltext – rozsekáme na slova, každé slovo musí padnout do aspoň jednoho OR pole (viz spec) */
    private String q;

    // Jednoduché přesné/contains filtry
    private String code;
    private ProjectStatus status;

    private UUID customerId;
    private UUID projectManagerId;

    /** true → pouze nearchivované (archivedAt IS NULL), false → pouze archivované; null → bez omezení */
    private Boolean active;

    // Rozsahy plánovaných dat (MVP – obě hranice jsou volitelné)
    private LocalDate plannedStartFrom;
    private LocalDate plannedStartTo;
    private LocalDate plannedEndFrom;
    private LocalDate plannedEndTo;

    // Rezerva: actualStart/End, currency, vatMode, tagy … dle potřeby
}
