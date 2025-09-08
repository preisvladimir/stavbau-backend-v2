alter table users add column if not exists token_version int not null default 0;
alter table users add column if not exists refresh_token_id uuid null;