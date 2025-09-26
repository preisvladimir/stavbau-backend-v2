-- Povolení rozšíření pro trigramové vyhledávání (vyžaduje práva superusera)
-- V dev (Docker/Testcontainers) to projde; ve spravované DB viz fallback níže.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
