--V20251015_19__users_add_profile_jsonb.sql
alter table users
 add column if not exists display_name text,
 add column if not exists avatar_url text,
 add column if not exists preferences jsonb not null default '{}'::jsonb;
 -- index pro případné dotazy nad preferencemi (volitelné)
 -- create index if not exists ix_users_prefs_gin on users using gin (preferences);