ALTER TABLE companies ALTER COLUMN ico SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_companies_ico ON public.companies (ico);
