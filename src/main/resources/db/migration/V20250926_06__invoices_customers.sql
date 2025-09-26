-- Vxx__invoices_customers.sql
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
CREATE INDEX IF NOT EXISTS idx_customers_name_trgm ON customers USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_customers_ico ON customers (ico);
-- Unikátnost IČO v rámci firmy (ignoruje NULL)
CREATE UNIQUE INDEX IF NOT EXISTS ux_customers_company_ico
    ON customers (company_id, ico)
    WHERE ico IS NOT NULL;

-- 2) invoices – doplnění vazby na customers a snapshot polí
-- POZN.: název tabulky 'invoices' předpokládá existující modul; pokud se liší, uprav dle skutečného názvu.
ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS customer_id UUID NULL,
    ADD COLUMN IF NOT EXISTS buyer_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS buyer_ico  VARCHAR(32),
    ADD COLUMN IF NOT EXISTS buyer_dic  VARCHAR(32),
    ADD COLUMN IF NOT EXISTS buyer_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS buyer_address JSONB;

-- vazba do customers: při smazání customeru necháme ve faktuře FK na NULL (historie drží snapshot)
ALTER TABLE invoices
    ADD CONSTRAINT IF NOT EXISTS fk_invoices_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON UPDATE RESTRICT ON DELETE SET NULL;

-- index pro analytiku a přehledy
CREATE INDEX IF NOT EXISTS idx_invoices_customer_id ON invoices (customer_id);

-- (volitelně) pokud chceme rychle filtrovat faktury podle názvu odběratele:
-- CREATE INDEX IF NOT EXISTS idx_invoices_buyer_name_trgm ON invoices USING GIN (buyer_name gin_trgm_ops);
