package cz.stavbau.backend.invoices.service;

import cz.stavbau.backend.features.invoices.model.NumberSeries;
import cz.stavbau.backend.features.invoices.repo.NumberSeriesRepository;
import cz.stavbau.backend.features.invoices.service.NumberSeriesService;
import cz.stavbau.backend.features.invoices.service.impl.NumberSeriesServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class NumberSeriesServiceTest {

    private NumberSeriesRepository repo;
    private NumberSeriesService service;
    private final UUID COMPANY = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setup() {
        repo = Mockito.mock(NumberSeriesRepository.class);
        service = new NumberSeriesServiceImpl(repo);
    }

    @Test
    void format_tokens_work() {
        String num = NumberSeriesServiceImpl.format("INV-{YYYY}-{MM}-{NNNN}", LocalDate.of(2025, 9, 12), 5);
        assertEquals("INV-2025-09-0005", num);
    }

    @Test
    void generate_creates_series_if_missing_and_increments() {
        LocalDate now = LocalDate.of(2025, 9, 12);

        // No row locked -> repo should fall back to default and create one (save called once for new row + once for increment)
        when(repo.lockByCompanyKeyYear(eq(COMPANY), eq("INV"), eq(2025))).thenReturn(Optional.empty());
        when(repo.findFirstByCompanyIdAndDefaultSeriesTrueOrderByCounterYearDesc(eq(COMPANY))).thenReturn(Optional.empty());

        // simulate save returning an entity with state
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArguments()[0]);

        String n1 = service.generateNextNumber(COMPANY, "INV", now);
        // Expected default pattern: "INV-{YYYY}-{NNNN}" -> INV-2025-0001
        assertEquals("INV-2025-0001", n1);
    }

    @Test
    void generate_uses_existing_series_and_increments_counter() {
        LocalDate now = LocalDate.of(2025, 1, 2);
        NumberSeries existing = new NumberSeries();
        existing.setId(UUID.randomUUID());
        existing.setCompanyId(COMPANY);
        existing.setKey("INV");
        existing.setPattern("INV-{YYYY}-{NNN}");
        existing.setCounterYear(2025);
        existing.setCounterValue(7);
        existing.setDefaultSeries(true);

        when(repo.lockByCompanyKeyYear(eq(COMPANY), eq("INV"), eq(2025))).thenReturn(Optional.of(existing));
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArguments()[0]);

        String n = service.generateNextNumber(COMPANY, "INV", now);
        assertEquals("INV-2025-008", n); // 7 -> 8, padded to 3
    }
}
