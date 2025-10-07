-- Přechod z 'site_address_json' -> 'site_address' a zajištění typu JSONB
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'projects' AND column_name = 'site_address_json'
  ) THEN
    ALTER TABLE projects RENAME COLUMN site_address_json TO site_address;
  END IF;
END$$;

-- Ujisti typ JSONB (funguje i při konverzi z text/json)
ALTER TABLE projects
  ALTER COLUMN site_address TYPE jsonb
  USING site_address::jsonb;
