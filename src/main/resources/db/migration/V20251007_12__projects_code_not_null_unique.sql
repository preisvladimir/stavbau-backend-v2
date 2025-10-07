-- Unikátnost (firma, code) – pokud ještě neexistuje
do $$
begin
  if not exists (
    select 1 from pg_indexes where schemaname = 'public' and indexname = 'ux_projects_company_code'
  ) then
    execute 'create unique index ux_projects_company_code on projects(company_id, code)';
  end if;
end$$;

-- NOT NULL (připravit data předem, v novém prostředí to projde rovnou)
alter table projects
  alter column code set not null;
