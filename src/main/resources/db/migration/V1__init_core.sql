create extension if not exists "uuid-ossp";

create table companies (
  id uuid primary key default uuid_generate_v4(),
  created_at timestamp, updated_at timestamp, created_by uuid, updated_by uuid,
  name varchar(255) not null,
  default_locale varchar(10) not null default 'cs'
);

create table users (
  id uuid primary key default uuid_generate_v4(),
  created_at timestamp, updated_at timestamp, created_by uuid, updated_by uuid,
  email varchar(320) not null unique,
  password_hash varchar(255) not null,
  company_id uuid not null references companies(id),
  locale varchar(10)
);

-- pro další sprinty: projects, *_translation, invoices...
