-- Registrations – hlavní tabulka pending registrací
-- File: V2025_10_16__create_registration_cases.sql

-- 0) Rozšíření pro case-insensitive e-maily
CREATE EXTENSION IF NOT EXISTS citext;

-- 1) Tabulka registration_cases
CREATE TABLE registration_cases (
    id                  UUID PRIMARY KEY,

    -- Stavová logika
    status              VARCHAR(32)  NOT NULL,
    next_action         VARCHAR(32)  NOT NULL,

    -- Komunikační údaje
    email               CITEXT       NOT NULL,

    -- Token pro ověření e-mailu (raw nikdy neukládáme; jen hash)
    token_hash          VARCHAR(255),
    token_expires_at    TIMESTAMPTZ,

    -- Anti-spam / resend
    cooldown_until      TIMESTAMPTZ,

    -- Idempotence na /start a /send-confirm (doporučeno hashovat už na BE)
    idempotency_key     VARCHAR(128),

    -- Draft firmy a souhlasy (GDPR)
    company_draft       JSONB        NOT NULL,
    consents            JSONB        NOT NULL,

    -- Meta o požadavku
    requested_ip        INET,
    user_agent          TEXT,
    locale              VARCHAR(16)  NOT NULL,

    -- ARES snapshot / metadata
    ares_lookup_meta    JSONB,

    -- Výsledek po potvrzení
    company_id          UUID,
    owner_user_id       UUID,

    -- Životní cyklus registrace (TTL)
    expires_at          TIMESTAMPTZ  NOT NULL,

    -- Audit + optimistický zámek (držet konzistenci s ostatními tabulkami)
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_by          VARCHAR(100),
    deleted_at          TIMESTAMPTZ,
    version             INT          NOT NULL DEFAULT 0,

    -- Ověření povolených hodnot (zůstáváme u VARCHAR + CHECK)
    CONSTRAINT chk_registration_status
        CHECK (status IN (
            'EMAIL_SENT','EMAIL_VERIFIED','APPROVED','COMPANY_CREATED',
            'EXPIRED','CANCELLED','FAILED'
        )),
    CONSTRAINT chk_registration_next_action
        CHECK (next_action IN ('VERIFY_EMAIL','NONE'))
);

-- 2) Cizí klíče (deferrable kvůli atomickému confirm kroku)
-- Pozn.: tabulky companies a users už v systému existují.
ALTER TABLE registration_cases
    ADD CONSTRAINT fk_registration_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE registration_cases
    ADD CONSTRAINT fk_registration_owner_user
        FOREIGN KEY (owner_user_id) REFERENCES users(id)
        DEFERRABLE INITIALLY DEFERRED;

-- 3) Indexy a omezení

-- Unikátní částečný index pro token_hash (jednorázovost a anti-reuse)
CREATE UNIQUE INDEX ux_registration_cases_token_hash_partial
    ON registration_cases(token_hash)
    WHERE token_hash IS NOT NULL;

-- Rychlá detekce „aktivních“ registrací pro e-mail (duplicitní snahy)
CREATE INDEX ix_registration_cases_email_active
    ON registration_cases(email, status)
    WHERE status IN ('EMAIL_SENT','EMAIL_VERIFIED','APPROVED');

-- Expirace celé registrace – pro cleanup joby a dotazy
CREATE INDEX ix_registration_cases_expires_at
    ON registration_cases(expires_at);

-- Expirace tokenu – pro resend/validace
CREATE INDEX ix_registration_cases_token_expires_at
    ON registration_cases(token_expires_at);

-- Obecný index na status (přehledy / operativní dotazy)
CREATE INDEX ix_registration_cases_status
    ON registration_cases(status);

-- (Volitelné) GIN indexy – odkomentovat pouze pokud budeme nad JSONB filtrovat
-- CREATE INDEX ix_registration_cases_company_draft_gin ON registration_cases USING GIN (company_draft);
-- CREATE INDEX ix_registration_cases_consents_gin      ON registration_cases USING GIN (consents);

-- 4) Komentáře (pomáhá v DB nástrojích)
COMMENT ON TABLE  registration_cases IS 'Pending registrace firmy a vlastníka (OWNER) se stavovým automatem, TTL a auditem.';
COMMENT ON COLUMN registration_cases.status           IS 'EMAIL_SENT|EMAIL_VERIFIED|APPROVED|COMPANY_CREATED|EXPIRED|CANCELLED|FAILED';
COMMENT ON COLUMN registration_cases.next_action      IS 'VERIFY_EMAIL|NONE – další očekávaná akce uživatele.';
COMMENT ON COLUMN registration_cases.email            IS 'Kontakt pro verifikaci; CITEXT (case-insensitive).';
COMMENT ON COLUMN registration_cases.token_hash       IS 'Hash ověřovacího tokenu (raw se nikdy neukládá).';
COMMENT ON COLUMN registration_cases.company_draft    IS 'JSONB draft firmy (ICO/VAT, název, adresa, country, locale, settings, preferences).';
COMMENT ON COLUMN registration_cases.consents         IS 'JSONB s důkazy souhlasů (timestamps, IP).';
COMMENT ON COLUMN registration_cases.expires_at       IS 'Globální TTL registrace (po uplynutí přechod na EXPIRED).';
COMMENT ON COLUMN registration_cases.company_id       IS 'Vytvořená Company po confirm (DEFERRABLE FK).';
COMMENT ON COLUMN registration_cases.owner_user_id    IS 'User, který se stal OWNER (DEFERRABLE FK).';
