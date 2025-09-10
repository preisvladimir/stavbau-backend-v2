-- INIT SCHEMA (companies, users, company_nace)

-- === companies ============================================================
CREATE TABLE IF NOT EXISTS companies (
  -- BaseEntity
  id UUID PRIMARY KEY,
  created_at TIMESTAMPTZ,
  updated_at TIMESTAMPTZ,
  created_by UUID,
  updated_by UUID,

  -- Company core
  ico VARCHAR(8) NOT NULL,
  obchodni_jmeno VARCHAR(255),
  pravni_forma_code VARCHAR(8),
  financni_urad_code VARCHAR(8),
  datum_vzniku DATE,
  datum_aktualizace_ares DATE,
  ares_last_sync_at TIMESTAMPTZ,

  -- NACE
  cz_nace_prevazujici VARCHAR(6),

  -- Geo/stat
  zakladni_uzemni_jednotka VARCHAR(16),
  okres_nuts_lau VARCHAR(16),
  institucionalni_sektor2010 VARCHAR(16),
  kategorie_poctu_pracovniku VARCHAR(8),

  -- ARES raw JSON
  ares_raw JSONB,

  -- RegisteredAddress (sídlo) – snake_case dle naming strategy
  kod_statu VARCHAR(2),
  nazev_statu VARCHAR(64),
  kod_kraje VARCHAR(4),
  nazev_kraje VARCHAR(64),
  kod_okresu VARCHAR(8),
  nazev_okresu VARCHAR(64),
  kod_obce VARCHAR(12),
  nazev_obce VARCHAR(128),
  kod_ulice VARCHAR(16),
  nazev_ulice VARCHAR(128),
  cislo_domovni VARCHAR(16),
  kod_casti_obce VARCHAR(12),
  nazev_casti_obce VARCHAR(128),
  kod_adresniho_mista VARCHAR(16),
  psc VARCHAR(16),
  textova_adresa VARCHAR(512),
  standardizace_adresy BOOLEAN,
  typ_cislo_domovni VARCHAR(4),

  -- DeliveryAddress (doručovací, prefix dor_*)
  dor_ulice VARCHAR(128),
  dor_cislo VARCHAR(16),
  dor_psc VARCHAR(16),
  dor_obec VARCHAR(128),
  dor_stat VARCHAR(64)
);

-- unikát na IČO
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ux_companies_ico') THEN
    ALTER TABLE companies ADD CONSTRAINT ux_companies_ico UNIQUE (ico);
  END IF;
END$$;

-- index na NUTS
CREATE INDEX IF NOT EXISTS ix_companies_nuts ON companies (okres_nuts_lau);

-- === company_nace (element collection) ====================================
CREATE TABLE IF NOT EXISTS company_nace (
  company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
  nace_code VARCHAR(6) NOT NULL,
  PRIMARY KEY (company_id, nace_code)
);
CREATE INDEX IF NOT EXISTS ix_company_nace_code ON company_nace (nace_code);

-- === users ================================================================
-- Sloupce podle chybové hlášky (seed vkládá tyto fieldy)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  created_at TIMESTAMPTZ,
  updated_at TIMESTAMPTZ,
  created_by UUID,
  updated_by UUID,

  company_id UUID NULL,
  email VARCHAR(255) NOT NULL,
  locale VARCHAR(16),
  password_hash VARCHAR(255) NOT NULL,
  refresh_token_id UUID,
  token_version INTEGER NOT NULL DEFAULT 0,

  CONSTRAINT users_company_id_fkey FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- email bývá unikátní
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ux_users_email') THEN
    ALTER TABLE users ADD CONSTRAINT ux_users_email UNIQUE (email);
  END IF;
END$$;
