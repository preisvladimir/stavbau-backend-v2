-- V20251010_15__company_members_add_archived_at.sql
ALTER TABLE company_members
  ADD COLUMN IF NOT EXISTS archived_at timestamptz;

CREATE INDEX IF NOT EXISTS idx_company_members_archived_at
  ON company_members(archived_at);