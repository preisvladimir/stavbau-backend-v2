-- Vytvoření číselných řad faktur
CREATE TABLE IF NOT EXISTS number_series (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    code          TEXT NOT NULL,      -- interní kód řady (např. 'INV')
    name          TEXT NOT NULL,      -- popisný název
    prefix        TEXT,               -- např. 'INV-2025-'
    suffix        TEXT,               -- např. '/A'
    next_value    BIGINT NOT NULL DEFAULT 1,  -- další číslo k vydání
    padding       INT    NOT NULL DEFAULT 4,  -- zero-pad (např. 0001)
    version       INT    NOT NULL DEFAULT 0,  -- optimistic locking
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- unikátní kód řady v rámci firmy
CREATE UNIQUE INDEX IF NOT EXISTS uq_number_series_company_code
ON number_series(company_id, code);
