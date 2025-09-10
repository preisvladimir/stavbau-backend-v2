-- V2025_09_10_002__fix_companies_kategorie_poctu_pracovniku.sql
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'companies' AND column_name = 'kategorie_poctr_pracovniku'
  ) THEN
    ALTER TABLE companies RENAME COLUMN kategorie_poctr_pracovniku TO kategorie_poctu_pracovniku;
  END IF;
END$$;
