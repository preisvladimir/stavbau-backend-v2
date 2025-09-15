CREATE TABLE IF NOT EXISTS company_members (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,

    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role       VARCHAR(32) NOT NULL
);

-- jeden uživatel je členem firmy max 1×
CREATE UNIQUE INDEX IF NOT EXISTS ux_company_members_company_user
  ON company_members(company_id, user_id);

-- v jedné firmě smí být právě jeden OWNER
CREATE UNIQUE INDEX IF NOT EXISTS ux_company_members_owner_unique
  ON company_members(company_id)
  WHERE role = 'OWNER';
