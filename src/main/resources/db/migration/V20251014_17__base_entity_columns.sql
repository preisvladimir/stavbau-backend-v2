-- V20251014_17__base_entity_columns.sql

-- Companies
alter table companies
  add column if not exists created_at timestamptz,
  add column if not exists updated_at timestamptz,
  add column if not exists created_by uuid,
  add column if not exists updated_by uuid,
  add column if not exists deleted boolean not null default false,
  add column if not exists deleted_at timestamptz,
  add column if not exists deleted_by uuid,
  add column if not exists version bigint not null default 0;

-- Customers
alter table customers
  add column if not exists created_at timestamptz,
  add column if not exists updated_at timestamptz,
  add column if not exists created_by uuid,
  add column if not exists updated_by uuid,
  add column if not exists deleted boolean not null default false,
  add column if not exists deleted_at timestamptz,
  add column if not exists deleted_by uuid,
  add column if not exists version bigint not null default 0;

-- Company Members
alter table company_members
  add column if not exists created_at timestamptz,
  add column if not exists updated_at timestamptz,
  add column if not exists created_by uuid,
  add column if not exists updated_by uuid,
  add column if not exists deleted boolean not null default false,
  add column if not exists deleted_at timestamptz,
  add column if not exists deleted_by uuid,
  add column if not exists version bigint not null default 0;

-- Projects
alter table projects
  add column if not exists created_at timestamptz,
  add column if not exists updated_at timestamptz,
  add column if not exists created_by uuid,
  add column if not exists updated_by uuid,
  add column if not exists deleted boolean not null default false,
  add column if not exists deleted_at timestamptz,
  add column if not exists deleted_by uuid,
  add column if not exists version bigint not null default 0;

-- (volitelné) Indexy pro sorty a rychlé filtry
create index if not exists ix_companies_created_at   on companies(created_at desc);
create index if not exists ix_customers_created_at   on customers(created_at desc);
create index if not exists ix_members_created_at     on company_members(created_at desc);
create index if not exists ix_projects_created_at    on projects(created_at desc);

create index if not exists ix_companies_deleted      on companies(deleted);
create index if not exists ix_customers_deleted      on customers(deleted);
create index if not exists ix_members_deleted        on company_members(deleted);
create index if not exists ix_projects_deleted       on projects(deleted);

-- optional: po naplnění dat můžeš odstranit defaulty (držím je, pokud ti to vyhovuje)
-- alter table ... alter column version drop default;
-- alter table ... alter column deleted drop default;
