-- V20251010_14__projects_add_canonical_name.sql

-- 1) Přidání sloupců (idempotentně)
ALTER TABLE projects
  ADD COLUMN IF NOT EXISTS name        text,
  ADD COLUMN IF NOT EXISTS description text;

-- 2) Jednorázový backfill z překladů:
--    preferuj 'cs' → pak 'en' → jinak první dostupný překlad
WITH pick_cs AS (
  SELECT pt.project_id, pt.name, pt.description
  FROM project_translations pt
  WHERE pt.locale ILIKE 'cs%'
),
pick_en AS (
  SELECT pt.project_id, pt.name, pt.description
  FROM project_translations pt
  WHERE pt.locale ILIKE 'en%'
),
pick_any AS (  -- CTE se může jmenovat pick_any, problém byl až v aliasu tabulky "any"
  SELECT DISTINCT ON (pt.project_id) pt.project_id, pt.name, pt.description
  FROM project_translations pt
  ORDER BY pt.project_id, pt.locale
),
chosen AS (
  SELECT
    p.id AS project_id,
    COALESCE(cs.name, en.name, pa.name)        AS tr_name,
    COALESCE(cs.description, en.description, pa.description) AS tr_desc
  FROM projects p
  LEFT JOIN pick_cs  cs ON cs.project_id = p.id
  LEFT JOIN pick_en  en ON en.project_id = p.id
  LEFT JOIN pick_any pa ON pa.project_id = p.id   -- <<< alias není "any", ale "pa"
)
UPDATE projects p
SET
  name        = COALESCE(p.name, c.tr_name, CONCAT('Project ', p.code)),
  description = COALESCE(p.description, c.tr_desc)
FROM chosen c
WHERE c.project_id = p.id;

-- 3) Guard: prázdné názvy nahraď placeholderem, aby šel nasadit NOT NULL
UPDATE projects
SET name = CONCAT('Project ', code)
WHERE name IS NULL OR btrim(name) = '';

-- 4) NOT NULL constraint (a případný index)
ALTER TABLE projects ALTER COLUMN name SET NOT NULL;

-- POZNÁMKA:
-- Pokud chceš fulltext-like hledání, připrav index s pg_trgm:
-- (nedávej CONCURRENTLY, protože Flyway zpravidla běží v transakci)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_projects_name_trgm ON projects USING gin (name gin_trgm_ops);

-- rozšíření
CREATE EXTENSION IF NOT EXISTS unaccent;

-- IMMUTABLE wrapper kolem unaccent (použij explicitní slovník)
CREATE OR REPLACE FUNCTION public.unaccent_immutable(text)
RETURNS text
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT public.unaccent('public.unaccent', $1)
$$;

-- výrazový trigram index: unaccent + lower
CREATE INDEX IF NOT EXISTS idx_projects_name_unaccent_trgm
  ON projects USING gin (public.unaccent_immutable(lower(name)) gin_trgm_ops);
