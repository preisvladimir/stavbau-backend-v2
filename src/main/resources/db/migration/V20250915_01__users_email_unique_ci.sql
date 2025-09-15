ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Case-insensitive unikátní index (PostgreSQL)
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_ci ON public.users (lower(email));
