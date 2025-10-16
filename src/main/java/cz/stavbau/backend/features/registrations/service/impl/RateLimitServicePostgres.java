package cz.stavbau.backend.features.registrations.service.impl;

import cz.stavbau.backend.features.registrations.config.RegistrationsProperties;
import cz.stavbau.backend.features.registrations.service.RateLimitService;
import cz.stavbau.backend.features.registrations.service.util.EmailHashUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.*;
import java.util.Objects;

public class RateLimitServicePostgres implements RateLimitService {

    private final JdbcTemplate jdbc;
    private final RegistrationsProperties props;
    private final Clock clock;

    public RateLimitServicePostgres(JdbcTemplate jdbc, RegistrationsProperties props, Clock clock) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.props = Objects.requireNonNull(props);
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    // ------- Public API --------

    @Override
    public void checkStartAllowance(String email, InetAddress ip) {
        String emailKey = keyEmailDay("start", email);
        String ipKey = keyIpHour("start", ip);
        int emailLimit = props.getRatelimit().getStart().getPerEmailPerDay();
        int ipLimit = props.getRatelimit().getStart().getPerIpPerHour();
        if (!consume(emailKey, windowDayStart(), Duration.ofDays(1), emailLimit)) {
            throw tooMany();
        }
        if (!consume(ipKey, windowHourStart(), Duration.ofHours(1), ipLimit)) {
            throw tooMany();
        }
    }

    @Override
    public void onStartCommitted(String email, InetAddress ip) {
        // Nic dalšího – počítadla jsme inkrementovali v checku.
    }

    @Override
    public void checkResendAllowance(String email, InetAddress ip) {
        String emailKey = keyEmailHour("resend", email);
        int emailLimit = props.getRatelimit().getResend().getPerEmailPerHour();
        if (!consume(emailKey, windowHourStart(), Duration.ofHours(1), emailLimit)) {
            throw tooMany();
        }
    }

    @Override
    public void onResendCommitted(String email, InetAddress ip) {
        // Nic – inkrement už proběhl.
    }

    // ------- Internals --------

    private boolean consume(String bucketKey, Instant windowStart, Duration windowTtl, int limit) {
        // 1) Pokud klíč existuje a window_start == aktuální okno, zkusíme ++count <= limit
        // 2) Pokud klíč neexistuje nebo window změna, resetujeme na 1 a nové window_start
        int updated = jdbc.update("""
            INSERT INTO rate_limit_counters (bucket_key, window_start, count)
            VALUES (?, ?, 1)
            ON CONFLICT (bucket_key) DO UPDATE
              SET count = CASE
                            WHEN rate_limit_counters.window_start = EXCLUDED.window_start
                            THEN rate_limit_counters.count + 1
                            ELSE 1
                          END,
                  window_start = CASE
                                   WHEN rate_limit_counters.window_start = EXCLUDED.window_start
                                   THEN rate_limit_counters.window_start
                                   ELSE EXCLUDED.window_start
                                 END
            """, bucketKey, Timestamp.from(windowStart));
        // updated je vždy 1 (insert/upsert)
        Integer current = jdbc.queryForObject(
                "SELECT count FROM rate_limit_counters WHERE bucket_key = ?",
                Integer.class, bucketKey
        );
        // TTL – necháme na cron/vacuum; případně lze přidat job na purge starých oken.
        return current != null && current <= limit;
    }

    private IllegalStateException tooMany() {
        // Mapování na HTTP 429 zajistí exception handler v PR5; tady jednoduchý typ.
        return new IllegalStateException("rateLimit.exceeded");
    }

    private Instant windowHourStart() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
        return zdt.withMinute(0).withSecond(0).withNano(0).toInstant();
    }

    private Instant windowDayStart() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
        return zdt.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private String keyEmailDay(String scope, String email) {
        return "reg:" + scope + ":email:day:" + EmailHashUtil.sha256Lower(email);
    }

    private String keyEmailHour(String scope, String email) {
        return "reg:" + scope + ":email:hour:" + EmailHashUtil.sha256Lower(email);
    }

    private String keyIpHour(String scope, InetAddress ip) {
        String ipStr = ip == null ? "unknown" : ip.getHostAddress();
        return "reg:" + scope + ":ip:hour:" + ipStr;
    }
}
