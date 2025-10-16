-- roles/scopes + archiving meta do company_members
alter table company_members
  add column if not exists roles         jsonb not null default '[]'::jsonb,
  add column if not exists scopes        jsonb not null default '[]'::jsonb,
  add column if not exists archived_by   uuid,
  add column if not exists archive_reason text;

-- backfill: enum role -> roles[0]
update company_members
set roles = jsonb_build_array(role::text)
where roles = '[]'::jsonb;

-- (volitelné) GIN indexy:
-- create index if not exists ix_members_roles_gin  on company_members using gin (roles);
-- create index if not exists ix_members_scopes_gin on company_members using gin (scopes);
