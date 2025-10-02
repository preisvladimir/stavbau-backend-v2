alter table companies
  add column default_locale varchar(8);

comment on column companies.default_locale is 'BCP-47 locale (e.g., cs-CZ, en)';

-- jednoduchý regex: "xx" nebo "xx-XX"
alter table companies
  add constraint ck_companies_default_locale
  check (default_locale is null or default_locale ~ '^[A-Za-z]{2}(-[A-Za-z]{2})?$');

-- seed pro stávající záznamy (lze změnit později v UI)
update companies set default_locale = 'cs-CZ' where default_locale is null;
