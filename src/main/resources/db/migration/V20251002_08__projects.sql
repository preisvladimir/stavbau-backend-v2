-- Projects core (company-scoped)
create table projects (
  id uuid primary key,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  created_by uuid,
  updated_by uuid,

  company_id uuid not null,
  customer_id uuid not null,
  project_manager_id uuid,

  code varchar(32) not null,
  status varchar(24) not null,

  planned_start_date date,
  planned_end_date date,
  actual_start_date date,
  actual_end_date date,
  archived_at timestamp with time zone,

  site_address_json jsonb,
  currency varchar(3),
  vat_mode varchar(16),
  tags text[],

  constraint uq_projects_company_code unique (company_id, code)
);

create index idx_projects_company on projects(company_id);
create index idx_projects_customer on projects(customer_id);
create index idx_projects_status on projects(status);
create index idx_projects_archived_at on projects(archived_at);

-- i18n
create table project_translations (
  project_id uuid not null references projects(id) on delete cascade,
  locale varchar(8) not null,
  name varchar(160) not null,
  description text,
  primary key (project_id, locale)
);

-- membership (project-level RBAC)
create table project_members (
  project_id uuid not null references projects(id) on delete cascade,
  user_id uuid not null,
  role varchar(32) not null,
  primary key (project_id, user_id)
);

create index idx_project_members_project on project_members(project_id);
