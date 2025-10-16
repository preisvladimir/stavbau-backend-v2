package cz.stavbau.backend.features.registrations.jobs;

import cz.stavbau.backend.features.registrations.config.RegistrationsProperties;
import cz.stavbau.backend.features.registrations.model.RegistrationCase;
import cz.stavbau.backend.features.registrations.repo.RegistrationCaseRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "registrations.jobs.expire", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RegistrationsExpireJob {

    private static final Logger log = LoggerFactory.getLogger(RegistrationsExpireJob.class);

    private final RegistrationCaseRepository repo;
    private final RegistrationsProperties props;
    private final Clock clock;

    // Metriky
    private final Counter expiredCounter;
    private final Counter runsCounter;
    private final Timer runTimer;

    @Value("${registrations.jobs.expire.batchSize:500}")
    private int configuredBatchSize;

    public RegistrationsExpireJob(RegistrationCaseRepository repo,
                                  RegistrationsProperties props,
                                  Clock clock,
                                  MeterRegistry meterRegistry) {
        this.repo = repo;
        this.props = props;
        this.clock = clock;

        this.expiredCounter = Counter.builder("registrations_expired_total")
                .description("Pocet registraci prepnuty na EXPIRED jobem")
                .register(meterRegistry);
        this.runsCounter = Counter.builder("registrations_expire_job_runs_total")
                .description("Spusteni RegistrationsExpireJob")
                .register(meterRegistry);
        this.runTimer = Timer.builder("registrations_expire_job_duration_seconds")
                .description("Doba behu expiračního jobu")
                .register(meterRegistry);
    }

    /**
     * Cron z properties; fallback na 10min.
     * Pozn.: @Scheduled neumí číst dynamicky přímo z objektu, proto placeholder.
     */
    @Scheduled(cron = "${registrations.jobs.expire.cron:0 */10 * * * *}")
    public void schedule() {
        runsCounter.increment();
        runTimer.record(this::expireOnceSafely);
    }

    private void expireOnceSafely() {
        try {
            int total = 0;
            int loops = 0;
            int maxLoops = 1000; // tvrdá brzda proti nekonečné smyčce

            int batchSize = configuredBatchSize;
            if (batchSize <= 0) batchSize = 500;

            while (loops++ < maxLoops) {
                int processed = expireBatchTransactional(batchSize);
                if (processed == 0) break;
                total += processed;
            }
            if (total > 0) {
                expiredCounter.increment(total);
                log.info("registrations.expire: expired={} loops={}", total, loops);
            }
        } catch (Exception e) {
            log.error("registrations.expire: FAILED", e);
        }
    }

    /**
     * Jedna transakční dávka – uzamkne kandidáty a přepne je na EXPIRED.
     */
    @Transactional
    public int expireBatchTransactional(int batchSize) {
        Instant now = Instant.now(clock);
        List<RegistrationCase> batch = repo.lockBatchForExpiration(now, batchSize);
        if (batch.isEmpty()) return 0;

        int count = 0;
        for (RegistrationCase rc : batch) {
            // Defenzivně: pokud mezitím někdo změnil stav na terminální/final, přeskoč
            String st = rc.getStatus();
            if ("COMPANY_CREATED".equals(st) || "EXPIRED".equals(st) || "CANCELLED".equals(st) || "FAILED".equals(st)) {
                continue;
            }
            rc.setStatus("EXPIRED");
            rc.setNextAction("NONE");
            rc.setTokenHash(null);
            rc.setTokenExpiresAt(null);
            rc.setUpdatedAt(now);
            repo.save(rc);
            count++;
        }
        return count;
    }
}
