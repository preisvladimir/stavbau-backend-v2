-- BaseEntity columns for users
alter table users
  add column if not exists created_at timestamptz,
  add column if not exists updated_at timestamptz,
  add column if not exists created_by uuid,
  add column if not exists updated_by uuid,
  add column if not exists deleted boolean not null default false,
  add column if not exists deleted_at timestamptz,
  add column if not exists deleted_by uuid,
  add column if not exists version bigint not null default 0;

-- backfill required timestamps if your entity uses nullable=false
update users
set created_at = coalesce(created_at, now()),
    updated_at = coalesce(updated_at, now());

-- helpful indexes
create index if not exists ix_users_deleted     on users (deleted);
create index if not exists ix_users_created_at  on users (created_at desc);
create index if not exists ix_users_company     on users (company_id);

