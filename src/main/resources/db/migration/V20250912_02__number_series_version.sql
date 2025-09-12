-- Add version column for optimistic locking and ensure helpful unique constraints
ALTER TABLE number_series ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;
-- Keep existing unique index on (company_id, counter_year, key)
