-- V20250926_06__invoices_customers.sql
-- STAVBAU-V2: Zavedení tabulky customers (invoices modul) + FK z invoices
-- Pozn.: žádný "down" skript, standard projektu je jen "up" migrace (viz guidelines).

-- 1) customers
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    created_by UUID,
    updated_by UUID,

    company_id UUID NOT NULL,

    -- základní identifikace odběratele
    type VARCHAR(16) NOT NULL DEFAULT 'ORGANIZATION', -- PERSON|ORGANIZATION (mapováno v aplikaci)
    name VARCHAR(255) NOT NULL,                        -- název/jméno pro fakturaci
    ico  VARCHAR(32),                                  -- IČO (CZ 8 znaků, ale necháme flexibilní)
    dic  VARCHAR(32),                                  -- DIČ / VAT ID

    -- kontakty
    email VARCHAR(255),
    phone VARCHAR(64),

    -- fakturační adresa (JSONB: {street, city, zip, country, ...})
    billing_address JSONB,

    -- platební podmínky a poznámky
    default_payment_terms_days INT,
    notes TEXT,

    -- volitelný link na users.user.id pro budoucí klientský portál
    linked_user_id UUID
);

-- indexy a unikátní omezení
CREATE INDEX IF NOT EXISTS idx_customers_company_id ON customers (company_id);
CREATE INDEX IF NOT EXISTS idx_customers_ico ON customers (ico);

-- Unikátnost IČO v rámci firmy (ignoruje NULL)
CREATE UNIQUE INDEX IF NOT EXISTS ux_customers_company_ico
    ON customers (company_id, ico)
    WHERE ico IS NOT NULL;

-- Bezpečná tvorba indexu pro vyhledávání podle jména:
-- - Pokud existuje pg_trgm a máme práva, použijeme GIN+gin_trgm_ops (nejrychlejší).
-- - Jinak fallback na btree LOWER(name), který funguje i bez rozšíření.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pg_trgm') THEN
    BEGIN
      CREATE EXTENSION IF NOT EXISTS pg_trgm;
    EXCEPTION WHEN insufficient_privilege THEN
      NULL;
    END;

    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_trgm') THEN
      CREATE INDEX IF NOT EXISTS idx_customers_name_trgm
        ON customers USING GIN (name gin_trgm_ops);
    ELSE
      CREATE INDEX IF NOT EXISTS idx_customers_name_btree
        ON customers (lower(name));
    END IF;

  ELSE
    CREATE INDEX IF NOT EXISTS idx_customers_name_btree
      ON customers (lower(name));
  END IF;
END
$$;

-- 2) invoices – doplnění vazby na customers a snapshot polí
-- POZN.: název tabulky 'invoices' předpokládá existující modul; pokud se liší, uprav dle skutečného názvu.
ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS customer_id UUID NULL,
    ADD COLUMN IF NOT EXISTS buyer_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS buyer_ico  VARCHAR(32),
    ADD COLUMN IF NOT EXISTS buyer_dic  VARCHAR(32),
    ADD COLUMN IF NOT EXISTS buyer_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS buyer_address JSONB;

-- vazba do customers: při smazání zákazníka necháme ve faktuře FK na NULL (historie drží snapshot)
DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1
      FROM information_schema.table_constraints tc
      WHERE tc.constraint_name = 'fk_invoices_customer'
        AND tc.table_name = 'invoices'
        AND tc.constraint_type = 'FOREIGN KEY'
  ) THEN
    ALTER TABLE invoices
      ADD CONSTRAINT fk_invoices_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON UPDATE RESTRICT ON DELETE SET NULL;
  END IF;
END
$$;

-- index pro analytiku a přehledy
CREATE INDEX IF NOT EXISTS idx_invoices_customer_id ON invoices (customer_id);

-- (volitelně) index pro buyer_name podle stejné logiky jako výše (pg_trgm/btree),
-- pokud bude potřeba vyhledávání i přes snapshotové jméno odběratele.
