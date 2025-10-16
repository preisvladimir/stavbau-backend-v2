-- Jednoduchý per-key counter pro rate-limit (tumbling okna)
CREATE TABLE IF NOT EXISTS rate_limit_counters (
    bucket_key      TEXT PRIMARY KEY,
    window_start    TIMESTAMPTZ NOT NULL,
    count           INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_rate_limit_window_start
    ON rate_limit_counters(window_start);
