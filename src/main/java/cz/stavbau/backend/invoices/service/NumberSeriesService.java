package cz.stavbau.backend.invoices.service;

import java.time.LocalDate;
import java.util.UUID;

public interface NumberSeriesService {
    /**
     * Vygeneruje další číslo faktury podle série (key) a data.
     * - Roční reset: pokud se rok změnil, counter se resetuje na 0 a nastaví se nový rok.
     * - Konkurence: chráněno PESSIMISTIC_WRITE lockem na řádce série.
     * - Tokeny: {YYYY}, {YY}, {MM}, {NN}, {NNN}, {NNNN} (zero-pad dle počtu N).
     *
     * @param companyId firma
     * @param seriesKey klíč série (např. "INV")
     * @param date datum (určuje rok/měsíc v patternu)
     * @return vygenerované číslo (unikátní v rámci firmy)
     */
    String generateNextNumber(UUID companyId, String seriesKey, LocalDate date);
}
