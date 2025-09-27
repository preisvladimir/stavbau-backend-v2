-- V20250927_07__customers_billing_address_jsonb.sql

DO $$
DECLARE
  tbl regclass;
BEGIN
  -- Najdi existující tabulku zákazníků v očekávaných schématech/názvech
  tbl := COALESCE(
           to_regclass('public.customer'),
           to_regclass('public.customers'),
           to_regclass('invoices.customer'),
           to_regclass('invoices.customers')
         );

  IF tbl IS NULL THEN
    RAISE EXCEPTION 'Customer table not found in expected schemas (public, invoices). '
                    'Please adjust migration to the actual table name.';
  END IF;

  -- Přidej nový JSONB sloupec, pokud chybí
  EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS billing_address JSONB', tbl);

  -- Best-effort backfill ze legacy textového sloupce (pokud existuje)
  IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name = split_part(tbl::text, '.', 2)
        AND table_schema = split_part(tbl::text, '.', 1)
        AND column_name = 'billing_address_json'
  ) THEN
    BEGIN
      -- Hromadně zkus přetypovat validní JSON; pokud by někde došlo k chybě, polkneme ji.
      EXECUTE format(
        'UPDATE %s
           SET billing_address = billing_address_json::jsonb
         WHERE billing_address IS NULL
           AND billing_address_json IS NOT NULL',
        tbl
      );
    EXCEPTION WHEN others THEN
      -- Fallback: nic – ponecháme legacy hodnotu v textu (řešíme později případně per-row)
      PERFORM 1;
    END;
  END IF;
END $$;
