-- (dev/test) pokud je potřeba pro gen_random_uuid():
-- create extension if not exists pgcrypto;

insert into company_members (
  id, company_id, user_id, role, roles,
  first_name, last_name, phone,
  created_at, updated_at, created_by, updated_by, deleted, version
)
select
  gen_random_uuid(), u.company_id, u.id,
  'MEMBER', jsonb_build_array('MEMBER'),
  null, null, null,
  now(), now(), u.created_by, u.updated_by, false, 0
from users u
where u.company_id is not null
  and not exists (
    select 1
    from company_members m
    where m.company_id = u.company_id and m.user_id = u.id
  );
