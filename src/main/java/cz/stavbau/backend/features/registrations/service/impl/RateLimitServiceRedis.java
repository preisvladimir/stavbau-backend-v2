package cz.stavbau.backend.features.registrations.service.impl;

import cz.stavbau.backend.features.registrations.config.RegistrationsProperties;
import cz.stavbau.backend.features.registrations.service.RateLimitService;
import cz.stavbau.backend.features.registrations.service.exceptions.RegistrationExceptions.RateLimited;
import cz.stavbau.backend.features.registrations.service.util.EmailHashUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetAddress;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class RateLimitServiceRedis implements RateLimitService {

    private final StringRedisTemplate redis;
    private final RegistrationsProperties props;
    private final Clock clock;

    // formáty okna: YYYYMMDD a YYYYMMDDHH (UTC)
    private static final DateTimeFormatter DAY   = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter HOUR  = DateTimeFormatter.ofPattern("yyyyMMddHH").withZone(ZoneOffset.UTC);

    public RateLimitServiceRedis(StringRedisTemplate redis,
                                 RegistrationsProperties props,
                                 Clock clock) {
        this.redis = Objects.requireNonNull(redis);
        this.props = Objects.requireNonNull(props);
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public void checkStartAllowance(String email, InetAddress ip) {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);

        String kEmail = key("start", "email:day", EmailHashUtil.sha256Lower(email), DAY.format(now));
        int emailLimit = props.getRatelimit().getStart().getPerEmailPerDay();
        if (!consume(kEmail, secondsUntilEndOfDay(now), emailLimit)) {
            throw new RateLimited();
        }

        String kIp = key("start", "ip:hour", ipStr(ip), HOUR.format(now));
        int ipLimit = props.getRatelimit().getStart().getPerIpPerHour();
        if (!consume(kIp, secondsUntilEndOfHour(now), ipLimit)) {
            throw new RateLimited();
        }
    }

    @Override
    public void onStartCommitted(String email, InetAddress ip) {
        // no-op (inkrementace proběhla v checku)
    }

    @Override
    public void checkResendAllowance(String email, InetAddress ip) {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
        String kEmail = key("resend", "email:hour", EmailHashUtil.sha256Lower(email), HOUR.format(now));
        int limit = props.getRatelimit().getResend().getPerEmailPerHour();
        if (!consume(kEmail, secondsUntilEndOfHour(now), limit)) {
            throw new RateLimited();
        }
    }

    @Override
    public void onResendCommitted(String email, InetAddress ip) {
        // no-op
    }

    // --- interní pomocné ---

    private boolean consume(String key, long ttlSeconds, int limit) {
        // Použijeme INCR + EXPIRE s ochranou, aby se EXPIRE nastavilo jen při prvním INCR.
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L && ttlSeconds > 0) {
            redis.expire(key, java.time.Duration.ofSeconds(ttlSeconds));
        }
        return count != null && count <= limit;
    }

    private String ipStr(InetAddress ip) {
        return ip == null ? "unknown" : ip.getHostAddress();
    }

    private String key(String scope, String dim, String id, String window) {
        // reg:<scope>:<dimension>:<id>:<window>
        return "reg:" + scope + ":" + dim + ":" + id + ":" + window;
    }

    private long secondsUntilEndOfHour(ZonedDateTime now) {
        ZonedDateTime end = now.withMinute(59).withSecond(59).withNano(999_000_000);
        return Math.max(1, java.time.Duration.between(now, end).toSeconds());
    }

    private long secondsUntilEndOfDay(ZonedDateTime now) {
        ZonedDateTime end = now.withHour(23).withMinute(59).withSecond(59).withNano(999_000_000);
        return Math.max(1, java.time.Duration.between(now, end).toSeconds());
    }
}
