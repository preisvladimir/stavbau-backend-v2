-- Projects: core descriptive + finance fields
ALTER TABLE projects
  ADD COLUMN name                 varchar(200) NOT NULL DEFAULT '',
  ADD COLUMN description          text NULL,
  ADD COLUMN type                 varchar(24) NULL,
  ADD COLUMN contract_value_net   numeric(18,2) NULL,
  ADD COLUMN contract_value_gross numeric(18,2) NULL,
  ADD COLUMN retention_percent    numeric(5,2)  NULL,
  ADD COLUMN payment_terms_days   int NULL,
  ADD COLUMN external_ref         varchar(64) NULL;

-- Backfill name from code for existing rows (so NOT NULL is satisfied)
UPDATE projects SET name = COALESCE(NULLIF(name, ''), code);

-- Clean up default if you want to enforce explicit values further on
ALTER TABLE projects ALTER COLUMN name DROP DEFAULT;

-- Useful indexes for filtering/sorting
CREATE INDEX IF NOT EXISTS ix_projects_company_name
  ON projects (company_id, name);

CREATE INDEX IF NOT EXISTS ix_projects_company_status
  ON projects (company_id, status);

CREATE INDEX IF NOT EXISTS ix_projects_company_customer
  ON projects (company_id, customer_id);

CREATE INDEX IF NOT EXISTS ix_projects_company_pm
  ON projects (company_id, project_manager_id);

CREATE INDEX IF NOT EXISTS ix_projects_company_planned_dates
  ON projects (company_id, planned_start_date, planned_end_date);
