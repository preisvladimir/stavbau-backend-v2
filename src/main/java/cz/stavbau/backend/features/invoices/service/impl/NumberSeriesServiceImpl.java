package cz.stavbau.backend.features.invoices.service.impl;

import cz.stavbau.backend.features.invoices.model.NumberSeries;
import cz.stavbau.backend.features.invoices.repo.NumberSeriesRepository;
import cz.stavbau.backend.features.invoices.service.NumberSeriesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class NumberSeriesServiceImpl implements NumberSeriesService {

    private final NumberSeriesRepository repo;

    public NumberSeriesServiceImpl(NumberSeriesRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public String generateNextNumber(UUID companyId, String seriesKey, LocalDate date) {
        int year = date.getYear();
        // Lock current year series row
        Optional<NumberSeries> opt = repo.lockByCompanyKeyYear(companyId, seriesKey, year);
        NumberSeries series = opt.orElseGet(() -> {
            // fallback: if not present for current year, try last default and "clone" semantics
            Optional<NumberSeries> latestDefault = repo.findFirstByCompanyIdAndDefaultSeriesTrueOrderByCounterYearDesc(companyId);
            NumberSeries ns = new NumberSeries();
            ns.setId(UUID.randomUUID());
            ns.setCompanyId(companyId);
            ns.setKey(seriesKey);
            ns.setPattern(latestDefault.map(NumberSeries::getPattern).orElse(seriesKey + "-{YYYY}-{NNNN}"));
            ns.setCounterYear(year);
            ns.setCounterValue(0);
            ns.setDefaultSeries(latestDefault.map(NumberSeries::isDefaultSeries).orElse(false));
            return repo.save(ns);
        });

        // Reset if year changed (shouldn't happen with lockByCompanyKeyYear, but safe-guard for legacy data)
        if (series.getCounterYear() != year) {
            series.setCounterYear(year);
            series.setCounterValue(0);
        }

        int next = series.getCounterValue() + 1;
        series.setCounterValue(next);
        repo.save(series); // persist increment

        return format(series.getPattern(), date, next);
    }

   public static String format(String pattern, LocalDate date, int counter) {
        String out = pattern;
        out = out.replace("{YYYY}", String.format("%04d", date.getYear()));
        out = out.replace("{YY}", String.format("%02d", date.getYear() % 100));
        out = out.replace("{MM}", String.format("%02d", date.getMonthValue()));

        // Handle variable N* tokens: {NN}, {NNN}, {NNNN}
        for (int n = 6; n >= 2; n--) {
            String token = "{" + "N".repeat(n) + "}";
            if (out.contains(token)) {
                out = out.replace(token, String.format("%0" + n + "d", counter));
            }
        }
        // Default: no token present -> append counter
        if (!out.contains("{N")) {
            out = out + "-" + counter;
        }
        return out;
    }
}
