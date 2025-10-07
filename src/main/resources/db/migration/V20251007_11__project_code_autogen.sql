-- Per-firma a per-rok čítač pro kódy projektů
create table if not exists project_code_counters (
  company_id uuid not null,
  year       integer not null,
  value      integer not null,
  primary key (company_id, year)
);

-- Zajistíme unikátnost kódu v rámci firmy (pokud ještě nemáš)
do $$
begin
  if not exists (
    select 1 from pg_indexes where schemaname = 'public' and indexname = 'ux_projects_company_code'
  ) then
    execute 'create unique index ux_projects_company_code on projects(company_id, code)';
  end if;
end$$;
