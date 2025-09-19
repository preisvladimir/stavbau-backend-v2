-- PR 2A: user state + invite flags (MVP)
ALTER TABLE public.users
  ADD COLUMN state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  ADD COLUMN password_needs_reset BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN invited_at TIMESTAMPTZ NULL;

-- (Volitelné) striktní kontrola hodnot stavu:
ALTER TABLE public.users
  ADD CONSTRAINT users_state_check
  CHECK (state IN ('INVITED','ACTIVE','DISABLED','LOCKED'));

-- Existující řádky jistíme na ACTIVE (pro případ, že by default neaplikoval):
UPDATE public.users SET state = 'ACTIVE' WHERE state IS NULL;
