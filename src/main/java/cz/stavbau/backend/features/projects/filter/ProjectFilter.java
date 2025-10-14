// src/main/java/cz/stavbau/backend/projects/filter/ProjectFilter.java
package cz.stavbau.backend.features.projects.filter;

import cz.stavbau.backend.features.projects.model.ProjectStatus;
import cz.stavbau.backend.features.projects.model.ProjectType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Web-binding friendly filtr pro projekty.
 * - LocalDate pole anotována @DateTimeFormat(iso = ISO.DATE) pro ?plannedStartFrom=2025-10-09
 * - business normalizace nepatří sem → řeší ProjectFilters.normalize(...)
 */
@Getter @Setter
public class ProjectFilter {
    /** Fulltext – rozseká se až ve Specification; tady jen čisté "q". */
    private String q;

    /** Přesný/contains filtr kódu projektu (normalizujeme trim→null). */
    private String code;

    /** Stav projektu (Spring umí mapovat z ?status=ACTIVE). */
    private ProjectStatus status;
    private ProjectType type;

    private UUID customerId;
    private UUID projectManagerId;

    /** true → pouze nearchivované (archivedAt IS NULL), false → pouze archivované; null → bez omezení */
    private Boolean active; // true => archivedAt is null; false => archived only

    // Rozsahy plánovaných dat (obě hranice volitelné; normalizace zajistí správné pořadí)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedStartFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedStartTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedEndFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedEndTo;

    // volitelně rozsahy ceny:
    private BigDecimal minContractValueNet;
    private BigDecimal maxContractValueNet;
}
