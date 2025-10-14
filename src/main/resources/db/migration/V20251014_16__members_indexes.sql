-- V20251014_16__members_indexes.sql
create index if not exists ix_members_company       on company_members (company_id);
create index if not exists ix_members_company_role  on company_members (company_id, role);
create index if not exists ix_members_archived_at   on company_members (archived_at);
create index if not exists ix_members_user          on company_members (user_id);