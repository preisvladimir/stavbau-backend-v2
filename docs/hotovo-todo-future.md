# 🗂 [hotovo-todo-future.md]()

## ✅ HOTOVO

### 8. 9. 2025 --- Inicializace projektu

-   **Maven projekt** (Java 17, Spring Boot 3.2.x) + základní `pom.xml`
    (web, security, validation, data-jpa, Flyway, PostgreSQL, MapStruct,
    OpenAPI).\
-   **Kostra aplikace**: `StavbauBackendApplication`, `application.yml`
    (datasource, Flyway, JWT dev secret).\
-   **i18n základ**: `MessageSourceConfig` (`messages_cs/en`),
    `AcceptHeaderLocaleResolver` (default cs).
    -   `PingController` + návrat hlavičky `Content-Language`.\
-   **Bezpečnost & infrastruktura**:
    -   `SecurityConfig`: stateless, CSRF off, povoleno `/actuator/**`,
        `/v3/api-docs/**`, `/swagger-ui/**`, `/api/v1/auth/**`,
        `/api/v1/ping`.\
    -   `ApiExceptionHandler` (RFC7807 styl).\
    -   `BaseEntity` + auditing (`JpaAuditingConfig`).\
-   **Databáze (Docker) & migrace**:
    -   Postgres 16 (Docker), síť + volume, předinstalace `uuid-ossp`.\
    -   Flyway:
        -   V1: `companies`, `users` (základ, locale, company_id).\
        -   V2: `users` rozšířeno o `token_version` a `refresh_token_id`
            (refresh rotace).\
-   **Oprava závislostí**: odstraněny konfliktní Hypersistence Utils
    (Hibernate 6.4).
    -   ponechána `hibernate-types-60` (JSONB apod.).\
-   **JWT autentizace (header) + refresh v HttpOnly cookie**
    -   `PasswordEncoder` (BCrypt, cost=12).\
    -   `JwtService`: vydávání access (krátká TTL) + refresh (aud
        „refresh", jti, ver).\
    -   `RefreshCookie`: HttpOnly, SameSite=Lax, path „/".\
    -   `JwtAuthenticationFilter`: načítá Bearer token →
        `UsernamePasswordAuthenticationToken`.\
    -   `AppUserPrincipal`: změněn na POJO (fix 500 na `/auth/me`).\
-   **AuthController**:
    -   `POST /auth/login` → access v těle + refresh cookie (rotace
        jti).\
    -   `POST /auth/refresh` → validace cookie (aud/ver/jti), rotace +
        reuse detekce.\
    -   `POST /auth/logout` → smaže cookie + zneplatní jti.\
    -   `GET /auth/me` → vrací `userId`, `companyId`, `email`.\
-   **Entity & repo**:
    -   `User` bez Lomboku (ruční gettery/settery).\
    -   `Company` + obě repository (`UserRepository`,
        `CompanyRepository`).\
-   **Dev seeding**:
    -   `DevUserSeeder` (profil dev\|default): firma + admin
        `admin@stavbau.local` / `admin123`.\
-   **Testy (PowerShell)**:
    -   `/api/v1/ping` (200, Content-Language).\
    -   `/auth/login` → získání access tokenu + refresh cookie.\
    -   `/auth/me` s Bearer.\
    -   `/auth/refresh` (nový access, cookie rotace).\
    -   `/auth/logout` (204).\
-   **Rate limiting (příprava)**:
    -   použití Bucket4j 7.6.0.\
    -   `RateLimitFilter` (per-IP, např. 10/min + 3/10s).\
    -   `SecurityConfig`: řetězení filtrů (rate-limit → JWT →
        UsernamePasswordAuthenticationFilter).
-   **GitHub repozitáře** založeny:
    - Backend: `https://github.com/preisvladimir/stavbau-backend-v2`
    - Frontend: `https://github.com/preisvladimir/stavbau-frontend-v2`
-   **Backend – první commit**: přidány dokumenty (`/docs`), `README.md`, `CHANGELOG.md`, `.gitignore`, základní BE skeleton (Spring Boot), Flyway migrace, i18n, security, JWT, rate-limit filtr (příprava).
- **Oprava remote**: backend byl omylem napojen na `frontend-v2`; remote opraven a obsah přesunut do správného repozitáře.
- **CI (backend)**: přidán workflow `backend-ci.yml` (Java 17 + Maven) a pushnut do `main`.
- **Repo metainfra – návrh**: připraveny `.gitattributes` (LF default) a `.editorconfig` (konzistentní formát); doporučeno commitnout.
- **Pokyny a šablony**: `STAVBAU_GUIDELINES.md`, `STAVBAU_TEMPLATES.md`, `POKYNY_GITHUB.md` a workflow šablony připraveny.

### 9. 9. 2025 --- Plánování RBAC BE (MVP)

- **🕒 Milník (plánování):** RBAC BE (MVP) – Step Plan schválen.
- **TODO (Sprint 2):**
    - Implementovat `security/rbac` (Scopes, CompanyRoleName, ProjectRoleName, BuiltInRoles).
    - `RbacService` + `RbacServiceImpl`, `RbacMethodSecurityConfig`.
    - Úpravy `AppUserPrincipal` a `JwtService` – claims: `companyRole`, `projectRoles[]`, `scopes[]`.
    - `/auth/me` rozšířit o `companyRole`, `scopes[]`.
    - Anotace pilotních endpointů (`projects:read`, `projects:create`).
    - Doplnit i18n klíče pro 401/403 (auth.forbidden_missing_scope).
    - Testy: unit (`BuiltInRolesTest`, `RbacServiceTest`), slice (`WebMvcTest` 401/403/200), integrační happy path.
- **FUTURE:**
    - Projektové role + `hasProjectScope` enforcement (Sprint 3).
    - DB perzistence rolí/scopes (PRO fáze).
    - Admin UI pro správu rolí.

### 9. 9. 2025 — RBAC základ + JWT filtry (BE)

**HOTOVO**
- Přidán skeleton RBAC modulu (`security/rbac`): `Scopes`, `CompanyRoleName`, `ProjectRoleName`, `ProjectRoleAssignment`, `BuiltInRoles` (prázdné mapy pro MVP), `RbacService` + `RbacSpelEvaluator`, `RbacMethodSecurityConfig`. :contentReference[oaicite:0]{index=0}
- `JwtService` rozšířen o RBAC claims (`companyRole`, `projectRoles[]`, `scopes[]`) + helpery `extract*`. :contentReference[oaicite:1]{index=1}
- `JwtAuthenticationFilter` refaktor: mapuje JWT → `AppUserPrincipal`; generuje `ROLE_*` a `SCOPE_*` authorities. :contentReference[oaicite:2]{index=2}
- `SecurityConfig` opraveno pořadí filtrů: **RateLimit → JWT → UsernamePasswordAuthenticationFilter** (oba ankory před vestavěný filtr).
- Aplikace startuje, autentizace běží (login/refresh), základ pro `@PreAuthorize("@rbac…")` připraven. :contentReference[oaicite:3]{index=3}

**TODO (Sprint 2)**
- Naplnit `BuiltInRoles.companyRoleScopes` podle RBAC_2.1 (OWNER, COMPANY_ADMIN, …). :contentReference[oaicite:4]{index=4}
- `/auth/me` rozšířit o `companyRole`, `projectRoles[]`, `scopes[]`; FE toggly budou čerpat z API. :contentReference[oaicite:5]{index=5}
- Anotovat pilotní endpointy: `projects:read`, `projects:create` přes `@PreAuthorize("@rbac.hasScope('…')")`. :contentReference[oaicite:6]{index=6}
- Testy: unit (`BuiltInRolesTest`, `RbacServiceImplTest`), slice (`@WebMvcTest` 401/403/200), integrační happy-path login → chráněný endpoint. :contentReference[oaicite:7]{index=7}
- i18n: doplnit klíče pro 401/403 (`auth.forbidden_missing_scope`, …).

**FUTURE**
- Project role enforcement (`hasProjectScope`, `canReadProject`) + membership check (Sprint 3). :contentReference[oaicite:8]{index=8}
- PRO fáze: RBAC v DB + admin UI, cache & invalidace. :contentReference[oaicite:9]{index=9}

## HOTOVO – 2025-09-10
- DB init přes Flyway: companies, company_nace, users (V2025_09_10_000)
- Doplňkové migrace: registered_address radek_adresy1/2 (V2025_09_10_001)
- Sjednocení názvů: tabulka `companies`, FK users.company_id → companies(id)
- MapStruct: vypnutý builder, ignorace auditních polí, AresCompanyMapper + CompanyMapper OK
- ARES integrace: DTO (AresSubjectDto), mapper, service skeleton, WebFlux v pom.xml
- AresCompanyMapper – sjednoceny ignore mapping pro single i legacy tvary payloadu.
- RegistrationStatuses: dočasně @Transient

## TODO (další sprint)
- AresClient+Service testy (MockWebServer), AresCompanyMapper testy
- Endpoint POST /api/companies/import/ares → persist & upsert
- Security pravidla pro `/api/companies/lookup/**`
- (Rozhodnout) Persist `RegistrationStatuses` – sloupce nebo JSONB snapshot

## FUTURE
- Validace IČO mod 11 (BE), FE hinty dle ARES
- Indexy pro vyhledávání: ico, okres_nuts_lau, dor_obec/psc (pokud bude potřeba)

### 11. 9. 2025 — Analýza & plán integrace GEO (Mapy.cz API)

**HOTOVO (analýza & plán):**
- Provedena analýza balíčku **geo.zip** z verze 1.
- Navržen **Step Plan** pro migraci do STAVBAU-V2: modular-by-feature, bezpečná konfigurace (API key v ENV), caching (Caffeine), testy (unit + integrační), FE hook (debounce input).

**TODO (implementace):**
- Vytvořit balíček `cz.stavbau.backend.geo` se strukturou `config/`, `service/`, `controller/`, `dto/`.
- Přidat `MapyCzProperties` + `application.yml` (`mapycz.*` s `${MAPYCZ_API_KEY}`).
- Implementovat `GeoConfig` (WebClient s timeouty, UA header, error filter).
- Dopsat `AddressSuggestion` (všechna pole) a mapper z odpovědi Mapy.cz.
- Opravit/doplnit `MapyCzGeoService.suggest(...)` – validace vstupů, normalizace `q`.
- Přidat cache layer (Caffeine) pro suggest.
- `GeoController` – `GET /api/geo/suggest`, zapojit rate-limit filtr.
- Testy jednotkové + integrační (ok/chyby/timeouty/edge cases).
- OpenAPI (schema DTO) + README pro geo modul.
- FE: `api.geo.suggest()` + debounce input (demo stránka „Projekt – adresa“).

**FUTURE (rozšíření):**
- Reverse geocoding (lon/lat → adresa).
- Geocoding přes více providerů (fallback).
- Perzistence „posledních výběrů“ pro UX.
- Validace PSČ podle země, normalizace diakritiky, detekce duplicit.
- Mapové widgety (piny, bbox zoom) v projektu a fakturaci.

### 11. 9. 2025 — Docker Compose + .gitignore pro GEO API key

- Přidán `docker-compose.yml` s předáním **MAPYCZ_API_KEY** do služby `backend`.
- Doplněna pravidla do `.gitignore` pro **.env** a **.env***.
- Pozn.: Compose načítá `.env` automaticky ze stejné složky jako `docker-compose.yml`.

### 12. 9. 2025 — GEO fix Swagger + Mapy.com
- GeoController: explicitní `@RequestParam(name=...)` → Swagger generuje `q/limit/lang` (ne arg0/1/2).
- maven-compiler: `<parameters>true</parameters>` kvůli názvům paramů.
- MapyCzClient: `/v1/geocode` + `query=`.
- GeoService: bbox z listu [minLon,minLat,maxLon,maxLat]; regionalStructure.isoCode.
- Smoke test /api/v1/geo/suggest OK.

### 12. 9. 2025 — Integrations/Weather (Meteostat RapidAPI)
- Přidán modul `integrations/weather` (WebClient, klient, service, controller).
- Endpoint: `GET /api/integrations/weather/summary?lat&lon&date[&alt]`.
- Účel: inline použití v Deníku (automatické doplnění počasí k záznamu).


### 12. 9. 2025 — Sprint 4: Finance & Dokumentace (MVP start)
**HOTOVO (plán):**
- Detailní Step Plan pro moduly Invoices & Files (BE/FE/DB/i18n/RBAC).
- Návrh DB schémat (Invoice, InvoiceLine, NumberSeries, StoredFile, FileTag, FileLink).
- API kontrakty v1 pro faktury a soubory.
- Akceptační kritéria + test plan.

**TODO (implementace):**
- [BE] Flyway migrace `invoices` + `files`.
- [BE] Services: NumberSeriesService, InvoiceService, InvoicePdfService, StoredFileService.
- [BE] Controllers + RBAC anotace + Swagger.
- [FE] Stránky /invoices a /files, formuláře, RBAC guardy.
- [FE] API klienti invoices/files, i18n texty.
- [QA] Unit/Integration/E2E testy, CI green.

**FUTURE (PRO rozšíření):**
- Verzování souborů, soft-delete/restore.
- Rozšířené číselné řady (více patternů, per projekt).
- Šablony PDF (branding per company), vícejazyčné PDF.
- S3/MinIO storage, AV scanning, signed URLs.

### 12. 9. 2025 — Fix WebClient kolize
- MapyCzClient nyní používá @Qualifier("geoWebClient"), aby nedocházelo ke kolizi s meteostatWebClient.

### 13. 9. 2025 — Sprint 4: Finance a dokumentace (Invoices & Files)

**HOTOVO**
- Přidán modul **Invoices**:
    - `NumberSeriesService` + unit testy (rezervace čísel, atomická transakce).
    - `InvoiceService` (CRUD, vystavení, změna stavu).
    - `InvoiceController` + DTOs + Swagger anotace.
    - Integrační test (`MockMvc`) pro základní akce.
- Přidán modul **Files**:
    - Entita `StoredFile`, `FileTag`.
    - Služba `StoredFileServiceImpl` + jednotkové testy.
    - `FileStorage` interface + implementace `LocalFsStorage` (bean).
    - REST API: upload, download, tag management (RBAC scopes `files:*`).
- RBAC:
    - Anotace endpointů `@PreAuthorize` s využitím `invoices:*`, `files:*` (dle RBAC_2.1).
- CI prošlo (backend build + testy zelené).

**TODO (další krok ve Sprintu 4):**
- Implementace `InvoicePdfService` (export faktur do PDF s i18n formátováním).
- Propojení faktur s ARES (automatické doplnění odběratele).
- FE demo: modul fakturace + file upload (propojení s BE API).
- Integrační testy `StoredFileController` (MockMvc: upload, download, 403 bez scope).

**FUTURE**
- Integrace se službou e-mailu: `InvoiceEmailService` (odeslání faktur zákazníkům).
- Rozšíření `FileStorage` o S3 implementaci (cloud).
- Verzionování souborů a archivace (PRO verze).

### 13. 9. 2025 — FE Auth MVP skeleton

**HOTOVO**
- Přidán skeleton autentizace na FE: `/login`, `AuthContext`, Axios klient + interceptory (TODO), guardy (`ProtectedRoute`, `ScopeGuard`), router a layout, i18n (common/auth/errors).
- Vytvořeny DTO typy: `LoginRequest/Response`, `RefreshRequest/Response`, `MeResponse`.

**TODO (další PR)**
- Implementace RHF + Zod validace ve `LoginPage`.
- Implementace interceptorů včetně refresh singleflight a 401→retry.
- Napojení `/auth/me` a naplnění `AuthContext` (user, role, scopes).
- UI toggle podle scopes v Sidebar/Projects.
- Unit & e2e testy dle plánu.

**FUTURE**
- Persist bez localStorage (rehydratace přes `/auth/me` po refreshi).
- HttpOnly cookie pro refresh (pokud BE umožní) + CSRF varianta.
- Captcha/slowdown při opakovaném 401/429.

### 14. 9. 2025 — FE Auth implementace (MVP)

**HOTOVO**
- LoginPage (RHF+Zod, i18n, loading, 401/429).
- Axios interceptory s refresh singleflight a 401→retry; 403/429 UX hooky.
- AuthContext napojen na /auth/me (po loginu) – naplnění user/role/scopes.
- RBAC toggly: Sidebar a Projects (button „Nový projekt“ jen se scope).
- Unit test: hasScope (anyOf/allOf), kostry pro guards/interceptors/e2e.

**TODO**
- Doplnit integrační test interceptorů (axios-mock-adapter).
- E2E: happy path login → dashboard, RBAC scénáře (Playwright/Cypress).
- UI rozšíření (toasty, show/hide password, vizuální stavy).
- Napojení reálných Projects API.

**FUTURE**
- Persist bez localStorage (volitelná rehydratace přes /auth/me).
- HttpOnly cookie refresh varianta (pokud BE povolí) + CSRF.
- Anti-bruteforce UX při 429 (cooldown/captcha).

### 14. 9. 2025 — FE Auth MVP + UI knihovna

**HOTOVO**
- FE autentizace:
    - `LoginPage` přepracován s **React Hook Form + Zod** validací, i18n hláškami, stavem loading, podporou 401/429.
    - Axios **interceptory** s refresh singleflight a retry pro 401, UX hooky pro 403/429.
    - **AuthContext** napojen na `/auth/me` – po loginu plní `user/role/scopes`.
    - RBAC toggly v **Sidebaru** a v Projects (scope `projects:create`).
- UI knihovna:
    - Základní komponenty sjednoceny: `Button`, `LinkButton`, `Badge`, `Card*` (Card, Header, Title, Description, Content, Footer).
    - Přidány helpery: `cn` utilita, `icons` index pro lucide-react.
    - Instalace a zapojení **class-variance-authority**, **clsx**, **lucide-react**.
    - Zavedeny design tokens (`sb-*` classes) pro konzistenci.
- i18n:
    - Struktura `i18n/` s namespacy `common`, `auth`, `errors`, `projects`.
    - Připojeno do providerů v `main.tsx`.

**TODO (další kroky Sprintu 2)**
- Integrační testy interceptorů (`axios-mock-adapter`).
- E2E testy login flow (happy path, RBAC scénáře) – Cypress/Playwright.
- UI rozšíření:
    - Toastery (shadcn/ui) místo fallback `console.log`.
    - Show/hide password toggle.
    - Lepší chybové/empty stavy.
- Napojení reálného **Projects API** (GET/POST).
- Dokončit CI pro frontend (lint, build, test).

**FUTURE**
- Persist stavů bez localStorage (rehydratace přes `/auth/me` po refreshi).
- Volitelná varianta s refresh tokenem v HttpOnly cookie + CSRF tokeny.
- UX při bruteforce/429 (cooldown, captcha).
- Rozšíření UI knihovny (`DataTable`, `Modal`, `EmptyState`) jako plnohodnotný „stavbau-ui“ balík pro všechny feature moduly.
- Konsolidace design tokens (`tokens.css`) a theming (dark mode).

## 🧭 Rozhodnutí architektury — 15. 9. 2025
**Téma:** Registrace firmy & členství (Sprint 2)  
**Rozhodnutí:** Zavedeme `CompanyMember` pro RBAC/membership (OWNER atd.). `User` zůstává štíhlý (auth). Kontaktní/fakturační údaje budou řešeny samostatným modulem **contacts/** a přes **invoices/Customer**. Připravíme migrační cestu `company_members.contact_id` (po zavedení contacts).  
**Důvod:** Čisté oddělení Auth vs. Business, soulad s modular-monolith by feature a RBAC 2.1, snížení reworku.  
**Dopady:** DB constraint „1 OWNER per company“, i18n klíče, rate-limit na public endpointu, bez autologinu (verifikace později).

## ✅ HOTOVO – 15. 9. 2025
- Schválen ADR: CompanyMember (MVP) + future Contacts/Customer.
- Upřesněna akceptační kritéria a test plan pro registraci firmy + OWNER.

## 🛠 TODO (Sprint 2/1 – BE)
- [ ] Flyway: `company_members` + unique owner per company, uniq `companies(ico)`, uniq `lower(users.email)`.
- [ ] Registrační služba: vytvořit Company, User (email+passwordHash+companyId), CompanyMember(OWNER).
- [ ] i18n: `company.exists`, `user.email.exists`, validační klíče (cs/en).
- [ ] MockMvc + @DataJpaTest: happy path, duplicity, unique OWNER, i18n.

## 🔭 FUTURE
- Contacts modul (Contact/Person + Address) a napojení `company_members.contact_id`.
- E-mail verifikace + autologin po potvrzení.
- Admin správa členů a rolí (team:* scopes).

## ✅ HOTOVO – 15. 9. 2025
- DB: unikátní index `lower(users.email)` a `companies(ico)`.
- DB: zavedena tabulka `company_members` + constraint „1 OWNER na firmu“.
- BE: `UserRepository` doplněn o `existsByEmailIgnoreCase` a `findByEmailIgnoreCase`.
- BE: `CompanyRepository` s `findByIco` a `existsByIco`.
- BE: přidána entita a repo `CompanyMember`.

## 🛠 TODO (Sprint 2/1 – registrace)
- [ ] Doplňit registrační službu: vytvoření `Company`, `User` (email+passwordHash+companyId), `CompanyMember(OWNER)`.
- [ ] Public endpoint `/api/v1/tenants/register` (permitAll + rate-limit).
- [ ] Integrační testy: happy path, duplicita IČO / e-mail, unikátní OWNER, i18n.

## ✅ HOTOVO – 16. 9. 2025
- BE registrace firmy: fungující endpoint `POST /api/v1/tenants/register` (public).
- Vytvoření Company, User (email+passwordHash+companyId), CompanyMember(OWNER).
- Opraven NPE: inicializace `Company.sidlo` před mapováním adresy.
- Ověřeno přes Swagger/cURL (201 Created).

## 🛠 TODO (Sprint 2/1 – BE)
- [ ] Dopsat integrační testy: 409 duplicitní IČO/e-mail, i18n, unique OWNER (DB).
- [ ] Omezit/odstranit DEV exception handler (detail DB chyb) mimo `local` profil.
- [ ] Nastavit rate-limit pro `/api/v1/tenants/register`.
- [ ] Swagger: doplnit `@Operation`, `@ApiResponse(409)` + example payloady.

## 🔭 FUTURE
- E-mail verifikace + autologin po potvrzení.
- Contacts modul (napojení na členy přes `contact_id`).

## 🛠 TODO – Sprint 2/2 (FE)
- [ ] FE Registration Wizard (3 kroky): ARES → náhled/edit → owner+submit.
- [ ] Validace (Zod): ico, company, address, owner, terms.
- [ ] API vrstva: `api/companies.aresLookup`, `api/tenants.registerTenant`.
- [ ] i18n cs/en (errors.*, validation.*, labels.*, steps.*).
- [ ] Error mapping: 409 company.exists/user.email.exists, 400 validation, 429 rate limit.
- [ ] UX: loading/disabled, retry, sessionStorage, a11y fokus.
- [ ] Testy: RTL (unit/integration) + e2e (happy/duplicitní scénáře).

## ✅ HOTOVO – 16. 9. 2025
- Schválen a připraven FE Step Plan pro registraci (3 kroky) vč. DTO, validací, i18n, UX a test plánu.

### 18. 9. 2025 — Team (Company Members) — BE skeleton
- **Přidáno:** TeamMembersController (POST/GET/PATCH/DELETE skeleton), DTO (`CreateMemberRequest`, `UpdateMemberRequest`, `MemberDto`, `MemberListResponse`), `TeamService` + `TeamServiceImpl` (stubs), `MemberMapper` (stub), WebMvcTest stub.
- **Security:** RBAC scopy a companyId guard **zatím ne** (půjde do PR 3/N).
- **i18n:** Seed klíče v `errors_cs/en`.
- **Swagger:** Tag `Team` + základní operace.
- **Dopad:** Bez DB změn; CI zelené.

## ✅ HOTOVO (19. 9. 2025)
- Zavedeno jednotné i18n API: `cz.stavbau.backend.common.i18n.Messages`.
- Zavedena hierarchie doménových výjimek: `DomainException`, `ConflictException`.
- Refactor `CompanyRegistrationServiceImpl` na `Messages` + `ConflictException`.
- Doplněny základní unit testy pro `Messages`.

## 📌 TODO
- Projít ostatní služby a nahradit lokální `msg()` + vnořené výjimky.
- Rozšířit `ApiExceptionHandler` o jednotné mapování všech `DomainException` s RFC7807.
- (Volitelné) Zavést `ErrorCode` enum a metodu `messages.msg(ErrorCode, args...)`.

## 💡 FUTURE
- Centralizovat validační kódy do `validation.properties` a sjednotit klíče napříč moduly.

### 19. 9. 2025 — Team (Company Members) — PR 2B (BE service)

- **Implementováno:** `TeamServiceImpl` (add/list/update/remove) + lokální helpery (normalizeEmail/validateEmail/requireTeamRole) + mapování **TeamRole→CompanyRoleName** (`ADMIN→COMPANY_ADMIN`, `MEMBER→VIEWER`).
- **Invite flow (MVP):** nový uživatel se zakládá se `state=INVITED`, `passwordNeedsReset=true`, `invitedAt=now()`, `passwordHash=BCrypt(random)`. `MemberDto.status` je odvozený (`INVITED|CREATED`).
- **Mapper:** `MemberMapper` čte jméno/telefon z `CompanyMember` (`firstName/lastName/phone`).
- **Guardy & konflikty:** 403 `errors.forbidden.company.mismatch` (companyId mismatch), 403 `errors.owner.last_owner_forbidden` (zákaz změny/odebrání OWNERa), 409 `member.exists`, 409 `user.assigned_to_other_company`, 404 `errors.not.found.member`.
- **i18n:** doplněno `errors.forbidden.company.mismatch` (cs/en) a `errors.validation.role.invalid`.
- **Security:** RBAC scopy `team:read|write` a controller guard na `{companyId}` budou řešené v **PR 3/N** (žádná změna `SecurityConfig` v tomto PR).
- **DB:** bez změn schématu; pokud chyběly sloupce `first_name/last_name/phone` u `company_members`, doplněn minor patch `V2025_09_19_002__company_member_contact_fields.sql`.
- **CI:** unit testy (invited flow, user v jiné firmě, OWNER guard) — **zelené**.

### 20. 9. 2025 — Sprint 2/1: Team (Company Members) — checkpoint

**Hotovo (BE)**
- PR 2A: Přidán stav uživatele a invite flagy
    - `users.state (INVITED|ACTIVE|DISABLED|LOCKED)`, `users.password_needs_reset`, `users.invited_at`.
    - `User` rozšířen o nové fieldy; JPA smoke test OK.
- PR 2B: Implementována business logika TeamService
    - `TeamServiceImpl` (add/list/update/remove), lokální helpery (normalizeEmail/validateEmail/requireTeamRole, generateRandomSecret).
    - Mapování **TeamRole → CompanyRoleName** (`ADMIN→COMPANY_ADMIN`, `MEMBER→VIEWER`).
    - Guardy a konflikty: `member.exists`, `user.assigned_to_other_company`, `errors.owner.last_owner_forbidden`, `errors.not.found.member`.
    - `MemberMapper` čte `firstName/lastName/phone` z `CompanyMember`.
    - (Pokud chybělo) mikro migrace `company_members.{first_name,last_name,phone}` doplněna.
- PR 3: Controller + RBAC + companyId guard
    - `TeamMembersController` (POST/GET/PATCH/DELETE) + `@PreAuthorize` (`team:read|team:write`).
    - `BuiltInRoles`: OWNER/COMPANY_ADMIN → read+write; VIEWER/AUDITOR_READONLY → read.
    - Company guard: path `{companyId}` vs principal.companyId (přes `@AuthenticationPrincipal`).
    - Swagger: sekce **Team** viditelná a běží.
    - Drobné výjimky: `NotFoundException`, `ForbiddenException` doplněny.
    - Oprava utilu/varianty pro `currentCompanyId()` (Optional nebo obalení v controlleru).

**Hotovo (i18n & errors)**
- Přidány/ujasněny klíče:
    - `errors.forbidden.company.mismatch` (cs/en),
    - `errors.validation.role.invalid`,
    - re-use: `errors.member.exists`, `errors.user.assigned_to_other_company`, `errors.owner.last_owner_forbidden`, `errors.not.found.member`, `errors.validation.email`.

**Hotovo (FE příprava)**
- Vyjasněna integrace FE skeletonu (PR 4/N) bez duplicit: použít `lib/api/client.ts`, sdílené typy v `lib/api/types.ts`.
- Připraven prompt pro nové vlákno: **PR 4/N — FE skeleton: Team** (route `/app/team`, TeamPage, TeamService nad existujícím klientem, i18n, msw, smoke test).

**Dopad na security**
- Aktivní scopy `team:read|team:write` + přiřazení k rolím v `BuiltInRoles`.
- CompanyId guard na všech Team endpointech (403 při mismatch).
- Rate-limit zatím **neaktivován** pro tyto endpointy (viz TODO).

---

**TODO (nejbližší)**
- **PR 3a:** zapnout rate-limit (např. 5/min) pro `POST /members` a `DELETE /members/{memberId}`; i18n `errors.rate.limit` + RFC7807 mapping na 429.
- **PR 4/N (FE skeleton):**
    - Route `/app/team` s `ProtectedRoute` + `ScopeGuard(['team:read'])`.
    - `TeamService` **nad** `lib/api/client.ts` (žádný nový Axios klient).
    - Typy **do** `lib/api/types.ts` (TeamRole, MemberDto, MemberListResponse, Create/UpdateMemberRequest).
    - `TeamPage` (tabulka, loading/empty/error).
    - i18n `team.json` (cs/en) + připojení do initu.
    - MSW handler GET (prázdný seznam) + smoke test.
- **PR 5/N (FE actions):** Add member (modal), Change role, Remove (confirm), error mapping (RFC7807→i18n), MSW pro POST/PATCH/DELETE, testy (unit + msw).
- **PR 6/N (E2E):** základní e2e scénář (login → /app/team → add → change role → remove), CI job.

**Future (po MVP)**
- Invite e-mail flow: invitation token + expirace, resend, aktivace účtu (endpoint), audit.
- Paging/sorting pro `GET /members` + filtr role.
- Konsolidace ProblemDetails (stálý `code` na BE, sdílený FE mapper).
- Audit log rozšířit (structured logging, korelace, metriky).
- Přechod na **contacts/**: `company_members.contact_id` + přesun osobních údajů (zpětně kompatibilní mapper).
- Rozšíření RBAC (jemné scopy `team:add|remove|update_role` pro PRO tarif).
- Swagger: doplnit příklady request/response (201/409/403/404/429) a kódy chyb.

### 20. 9. 2025 — RBAC claims a jemné scopy

- **RBAC:** Naplněny jemné scopy `team:add`, `team:remove`, `team:update_role` atd. v `BuiltInRoles`.
- **Login:** Upraven `AuthController.login()` – access token nyní při zapnutém `rbacClaimsEnabled` obsahuje `companyRole` a `scopes[]` (fallback na legacy varianta beze změny).
- **/auth/me:** Rozšířeno o `companyRole`, `projectRoles[]`, `scopes[]`.
- **Repo:** Zavedeno vyčtení role uživatele z `CompanyMember` (varianta B: načtení entity a čtení z pole `role`).
- **Docs:** Aktualizováno `RBAC_2.1_STAVBAU_V2.md` (scopes, mapping).
- **Výsledek:** `/auth/me` vrací kompletní RBAC kontext; login je stabilní i při chybách v RBAC části (fail-safe fallback).

### 21. 9. 2025 — Konvence ID a /auth/me rozšíření

- **BE:** Upraveno `MeResponse` (pole `id` místo `userId`) a příslušný controller.
- **Docs:** Doplněn odstavec o konvenci ID (UUID pouze `id`) do `STAVBAU_GUIDELINES.md`.
- **OpenAPI:** Snippet `/auth/me` aktualizován – `id`, `companyId`, `companyRole`, `projectRoles[]`, `scopes[]`.
- **Dopad:** Konsolidace ID konvencí pro všechny entity a DTO → do budoucna nebude nutný rework.

### 21. 9. 2025 — RBAC & AuthService refactor

- **RBAC rozšíření**
    - Přidány jemné scopy `team:add`, `team:remove`, `team:update_role`.
    - Rozšířen `BuiltInRoles.COMPANY_ROLE_SCOPES` podle návrhu z `RBAC_2.1_STAVBAU_V2.md`.
    - `/auth/me` nyní vrací i `companyRole`, `projectRoles[]`, `scopes[]`.

- **AuthController → AuthService**
    - Vytvořena servisní třída `AuthService` (metody `login`, `refresh`, `logout`, `buildMeResponse`).
    - Controller refaktorován na tenkou vrstvu – pouze deleguje na `AuthService`.
    - Zavedeno DTO `RefreshResponse` pro konzistentní odpověď `/auth/refresh`.
    - Návratové typy z `AuthService` refaktorovány: místo celého `Set-Cookie` header stringu vrací čistý `cookieValue` a DTO (`AuthResponse` / `RefreshResponse`).
    - V `AuthController` tím odpadl hack se `substring("Set-Cookie: ".length())`.

- **Konzistence ID**
    - Zavedena konvence: všechny entity dědí z `BaseEntity` (`id: UUID`).
    - Upraveno `MeResponse` a `AuthController` – FE dostává jednotně pole `id`.
    - Doplněna dokumentace do `STAVBAU_GUIDELINES.md` + snippet do `openapi.yml`.

## 2025-09-21 — PR 4/N — FE skeleton: Team / Company Members

### HOTOVO
- **Router:** přidána chráněná route `/app/team` (ProtectedRoute + ScopeGuard `required=['team:read']`, bez nové router instance).
- **TeamPage:** skeleton (stavy `loading / empty / error`, tabulka: *E-mail*, *Role* — company role z BE, *Jméno*, *Telefon*); čtení `companyId` z Auth (`useAuthContext`).
- **API typy (centrálně):** `lib/api/types.ts` rozšířeno o `TeamRole`, `CompanyRole`, `MemberDto`, `MemberListResponse`, `CreateMemberRequest`, `UpdateMemberRequest`.
- **TeamService:** `features/team/api/team.service.ts` (pouze existující `lib/api/client.ts`; metody `list/add/update/remove`; normalizace `memberId → id`; guard na `cancel`/`abort`; sdílené mapování chyb).
- **Sdílené mapování chyb:** `lib/api/problem.ts` (`toApiProblem`, `ApiError`, `mapAndThrow`) a zapojení v TeamService.
- **i18n:** přidán namespace `team` (cs/en) + registrace v `src/i18n/index.ts`.
- **MSW:** handler `GET /api/v1/tenants/:companyId/members` → `{ items: [] }`; agregace v `mocks/handlers`, worker v DEV, `setupTests.ts` pro Vitest.
- **Testy:** smoke test `TeamPage` (render title „Tým“) s mock Auth (scopes `['team:read']`, `companyId`).

### DoD / ověřeno
- Build + testy + MSW **OK**; `/app/team` je chráněná a načte prázdný seznam.
- **Bez nového Axios klienta** (použit `lib/api/client.ts`), typy jsou **centrálně** v `lib/api/types.ts`, error-mapper je **sdílený** v `lib/api/problem.ts`.
- **Žádné duplicity** souborů vůči stávajícímu repo stavu.
- Commity dle **Conventional Commits**, PR popis + štítky + **CODEOWNERS**.

### TODO (PR 5/N)
- Akce na TeamPage: **add/remove/update role** (scope `team:write`), formuláře a validace.
- MSW + testy pro **POST/PATCH/DELETE**; zobrazení field-errors z BE.
- i18n labely pro company role/status; UI badge pro `status`.
- (Volitelně) mapování `CompanyRole → i18n` na jednom místě.

### FUTURE
- Paging/filtrace/řazení + hledání (email/jméno).
- Detail člena + proklik na profil.
- Contract testy FE/BE a E2E flow.
- Výkon: virtualizace tabulky pro velké seznamy.

---

## 🔜 TODO (další sprint)

- **Testy**
    - Unit testy: `BuiltInRolesTest`, `AuthServiceTest` (happy path login/refresh, 401 při špatném hesle, 403 při scope chybě).
    - Slice testy: `@WebMvcTest` pro `AuthController` (`/login`, `/refresh`, `/me`).
    - Integrační testy: end-to-end login → refresh → přístup k chráněnému endpointu.

- **i18n**
    - Přidat klíče `auth.invalid_credentials`, `auth.refresh_revoked`, `auth.forbidden_missing_scope`.

---

## 💡 FUTURE

- Migrace RBAC do DB (`role_definitions`, `scope_definitions`, `role_scopes`).
- FE hooky: `useHasScope(scope)`, `ScopeGuard`.
- Admin UI pro správu rolí a scopes.

## 2025-09-21 — FE Team / Layout / UI (PR 4/N + část 5/N)
✅ Hotovo

Routing & Guards

Přidána route /app/team v src/routes/router.tsx přes ProtectedRoute + ScopeGuard(['team:read']).

Opraveno API ScopeGuard (prop anyOf), sjednocené použití.

Auth

useAuth()/useAuthContext() bez state wrapperu; čtení user.companyId.

Typy (lib/api/types.ts)

TeamRole = 'ADMIN' | 'MEMBER'.

CompanyRole (OWNER, COMPANY_ADMIN, …, SUPERADMIN).

MemberDto, MemberListResponse, CreateMemberRequest, UpdateMemberRequest, UpdateMemberRoleRequest.

API klient

features/team/api/team.service.ts (bez nového axios klienta; používá lib/api/client.ts), normalizace payloadů z BE.

Sdílené mapování chyb lib/api/problem.ts + ApiError.

TeamPage (features/team/pages/TeamPage.tsx)

Skeleton tabulky (email, role, jméno, telefon) + loading/empty/error.

Add Member panel (email, role, jméno, příjmení, telefon) + tolerantní validace e-mailu.

Update Role (inline select) s FE/BE guardy:

Nelze nastavovat SUPERADMIN ani OWNER z tenant UI.

Nelze měnit roli člena s OWNER (hlídá FE i BE).

FE ochrana „last owner“ (nelze „sundat“ posledního vlastníka).

Edit Profile přes MemberEditModal (PATCH detailu člena; připraveno na rozšiřování polí).

Delete Member (guard posledního OWNERa + chybové stavy).

Integrace DataTable (@/components/ui/stavbau-ui) + akční sloupec s ikonami.

UI používá Button a ikony z @/components/icons.

Zobrazení deleteError inline pod řádkem (fix „never read“).

i18n

team.json (cs/en): title, columns, actions, errors (notAssignable, onlySuperadminOwner, lastOwner) a placeholdery.

MSW

Základní handler GET /api/v1/tenants/:companyId/members (prázdný/ukázkový list) zapojen do mocks/handlers.

UI/UX – společné komponenty

SearchInput (aliasy ikon, default styly pro left/right ikonu).

patterns.ts (EMAIL_INPUT_PATTERN, EMAIL_REGEX, EMAIL_REGEX_STRICT) a sjednocené používání.

Sidebar: aktivní stav + „sticky“ indikátor (pseudo-element), util getNavClass({isActive}), a11y + i18n.

AppLayout: integrace prvků z v1 bez duplicit

FabProvider, MobileFab (≤ md), MobileBottomBar (≤ md).

TopbarActions slot (desktop) – jediný zdroj pravdy pro stránkové akce.

Topbar upraven, Sidebar zachován.

RBAC scopy (FE)

Add: team:write | team:add

Update role: team:write | team:update_role

Delete: team:write | team:remove

Read: team:read

🧪 Test/Infra

Založen smoke test pro TeamPage (render title) – (doplnit, pokud ještě není).

Ignorování cancel chyb (Axios/Abort/ApiError) ve všech efektech.

📝 BE poznámky (sladěno s FE)

POST (přidání): @PreAuthorize("@rbac.hasAnyScope('team:write','team:add')").

PATCH role: @PreAuthorize("@rbac.hasAnyScope('team:write','team:update_role')"), zákaz změny OWNER, validace role.

PATCH detailu membera: navrženo (jméno/příjmení/telefon) – sladěno s FE modalem.

DELETE: zákaz smazání posledního OWNERa, guard companyId; scopy: team:write | team:remove.

⏭ Todo (další PR 5/N kroky)

MSW: doplnit handlery POST/PATCH/DELETE + field-errors.

Testy: integrační testy (add/update/delete), i18n klíče, a11y (axe).

TeamPage: badge pro status, centralizované mapování CompanyRole → i18n.

Topbar: volitelně rozšířit „right actions“ slot pro více tlačítek (multi-fab).

🔭 Future

DataTable v2: server-side paging/sorting/filters, column visibility, density, toolbar, export, mobilní „cards“ layout, virtualizace (dle potřeby). → Zahájeno novým vláknem (Step Plan připraven).

Form validace: společný useZodForm/useForm helper (podle potřeby).

RBAC FE: centralizovat mapování scopů → UI capabilities.

## ✅ HOTOVO – 22. 9. 2025
- FE test runner: přidán `vitest.config.ts` s aliasem `@ -> ./src` a `vite-tsconfig-paths`.
- Vite config sjednocen s aliasy.
- Importy upraveny na explicitní soubory (`empty-state`), sjednocen název `datatable.tsx`.

## ▶️ TODO
- Ověřit `npx vitest --config vitest.config.ts`.
- Po průchodu testů navázat **PR 2 – Sorting + MSW demo**.

## ✅ HOTOVO – 22. 9. 2025
- FE testy: přidán testovací i18n init (src/test/i18n.ts) + import v setupTests; odstraněno varování NO_I18NEXT_INSTANCE.

## ▶️ TODO
- PR 2 – Sorting (controlled/uncontrolled) + MSW demo (header kliky, aria-sort, testy).

## ✅ HOTOVO – 22. 9. 2025
- FE – DataTable v2 (PR 2/5): Sorting (controlled/uncontrolled) přes TanStack.
    - Header interakce (klik/shift-klik/klávesy), aria-sort indikace.
    - i18n klíče `datatable.sort.*`.
    - Testy: cyklování řazení + ověření pořadí.
    - MSW: demo handler `/api/v1/demo/list` se `sort[]`.

## ▶️ TODO
- PR 3 – Paging (server/client) + pager komponenta + testy.

## ✅ HOTOVO – 22. 9. 2025
- Revert DataTable v2 (f2a0a6f..HEAD) proveden na větvi hotfix/revert-datatable a odeslán jako PR.

## ▶️ TODO
- Merge PR do main a ověřit `npm ci && npm run dev`.
- Nastavit branch protection na main.
- Založit `feature/datatable-v2` a doručit PR 1 (TanStack wrapper) izolovaně od runtime.

## ✅ HOTOVO – 23. 9. 2025
- Test stack: doinstalován vitest@^3, jsdom@^25, vite-tsconfig-paths@^5.
- tsconfig.types doplněn o "vitest/globals"; restart TS serveru.

## ▶️ TODO
- Spustit `npx vitest` (ověřit).
- Poté PR 1 (DataTableV2 shell) + mini test.

## ✅ HOTOVO – 22. 9. 2025
- FE – DataTable v2 (PR 1 restart): Přidán bezpečný shell nad @tanstack/react-table.
    - Bez i18n/MSW, bez dopadu na běh appky.
    - Základní test (render/skeleton/rows).
    - Node & CI sjednocení, lockfile regenerován, testy OK.

## ▶️ TODO
- PR 2 – Sorting (controlled/uncontrolled) + a11y (aria-sort).
- PR 3 – Paging (server/client) + Pager.
- PR 4 – Toolbar (search, visibility, density, i18n klíče).
- PR 5 – Row actions slot + příklad integrace (TeamPage).

## ✅ HOTOVO – 23. 9. 2025
- DataTableV2 PR2: opraveno volání toggleSorting (shift předáván jako multi, ne desc).
- Test `datatable-v2.sort.spec.tsx` prochází (aria-sort cyklus OK).

## ▶️ TODO
- PR 3 – Paging (server/client) + Pager komponenta + testy.

## ✅ HOTOVO – 23. 9. 2025
- FE – DataTableV2 (PR 3): Paging (client/server), pager UI, a11y.
- Testy: client paging (2/strana, navigace), controlled režim (onPageChange + rerender).

## ▶️ TODO
- PR 4 – Toolbar: search, column visibility, density; připravit i18n klíče.

## ✅ HOTOVO – 23. 9. 2025
- FE – DataTableV2 (PR 4): Toolbar (search state, column visibility, density).
- i18n: přidány klíče common.datatable.* (cs/en).
- Testy: visibility toggle, density toggle.

## ▶️ TODO
- PR 4.1 – Toolbar pokračování: page size selector (5/10/20), reset filtrů, export CSV (volitelné).
- PR 5 – Row actions slot + integrace v TeamPage.

## ✅ HOTOVO – 23. 9. 2025
- FE – DataTableV2 (PR 4.1): page size selector (5/10/20) + Reset filtrů.
- Testy: page size (client/server), reset stavů.

## ▶️ TODO
- PR 5 – Row actions slot + integrace (TeamPage).
- PR X – (volitelně) export CSV v Toolbaru.

## ▶️ TODO
- PR 5 – Row actions slot + integrace v TeamPage (guardy, a11y).

## ✅ HOTOVO – 23. 9. 2025
- FE: TeamPageV2 – plná integrace DataTableV2 (toolbar, client paging, row actions).

## ▶️ TODO (další PR)
- PR 6: per-row async stav (spinner/disable) sjednotit přes helper (useAsyncAction) + toast pattern.
- PR 7: server-side režim (page, sort, filters) napojit na TeamService.list s query parametry.
- PR 8: přepnout column visibility trigger z <details>/<summary> na <button> + popover (lepší a11y).

## ✅ HOTOVO – DataTableV2 theme toggle
- props: variant: 'surface' | 'plain' (default 'plain')
- props: className: string
- shell aplikuje variantu na wrapper (card/border/zebra pro 'surface')

## ✅ HOTOVO – DataTableV2 Toolbar: SearchInput (preset v1)
- Nahrazen plain <input> → <SearchInput /> z UI kitu
- A11y: ariaLabel, placeholder (i18n-ready)
- Vizuál: v1 preset = shoda s původním vzhledem

## ✅ HOTOVO – UI Select (native)
- Nová komponenta <Select /> v stavbau-ui (a11y-first, mobile-friendly).
- API: value/defaultValue/onChange, options|children, size, variant, icons, error/helper.
- Integrace: DataTableV2 Toolbar – „Počet na stránku“ používá <Select />.

## ▶️ FUTURE
- Volitelná „listbox“ varianta (custom popover) pro speciální případy.
- Virt. dlouhých seznamů (do 1k+ položek) – až bude potřeba.
- label/description props přímo v Select (interní <label>)

## ✅ HOTOVO — DataTableV2 (23. 9. 2025)

### Funkcionalita
- **Základní shell**
    - Plně typovaná generická komponenta `DataTableV2<T>`
    - Podpora variant vzhledu: `plain` a `surface`
    - Responsivní chování, konzistentní design se zbytkem `stavbau-ui`

- **Toolbar**
    - 🔍 **SearchInput** (náš vlastní) s i18n texty
    - 👁 **ColumnVisibilityMenu** s podporou variant (`details`/`popover`)
    - 🔢 **PageSize Select** – počet záznamů na stránku (napojený na náš `Select`)
    - 📏 **DensitySelect** – výběr hustoty řádků (`compact`, `cozy`, `comfortable`)
    - 🔄 **Reset filters** tlačítko (resetuje stav tabulky)
    - 📊 Indikátor stránky `p / c`

- **Hlavní tabulka**
    - Sorting (cyklus none → asc → desc, shift-click = multi-sort)
    - Paging (`page`, `pageSize`, `pageCount`, prev/next)
    - Row click handler (`onRowClick`)
    - Slot pro **rowActions** (např. ikony pro editaci/smazání)
    - EmptyState (včetně i18n textů)
    - Skeleton loading stavy

- **UX / i18n**
    - Všechny texty přes `react-i18next` (`common.json`)
    - Přístupnost: aria atributy (`aria-sort`, `aria-label`, `aria-live`)
    - Testy: unit testy pro shell, sorting, toolbar, actions

---

## 🛠️ FUTURE — DataTableV2

- [ ] **Server-side režim** (props `manualSorting`, `manualPaging`, API integrace)
- [ ] **Persistované preference uživatele** (uložení sloupců/density/pageSize do localStorage nebo BE)
- [ ] **Exporty** (CSV, XLSX, PDF)
- [ ] **Drag & drop reordering** sloupců
- [ ] **Inline editace buněk** (RHF + validace)
- [ ] **Filtrace per-column** (dropdowny, datumové range, multiselect)
- [ ] **Virtualizace řádků** (pro velké datasety)
- [ ] **Dark mode ladění** (ověřit kontrasty pro všechny varianty)

## 🟨 TODO — DataTableV2 – Responsive (Hybrid)

**Cíl:** Přidat responzivní vzhled DataTableV2 bez změny funkčnosti:
- `<md` (mobil): stacked cards (Title, Subtitle, 3–5 detailů, akce).
- `md–lg`: scrollable tabulka se sticky klíčovými sloupci.
- `≥lg`: beze změny (plná tabulka).

**Step Plan:**
1) API pro “card fields” (bez UI změn)
    - Sloupce: `priority`, `isTitle`, `isSubtitle`, `mobileHidden`, `formatter`.

2) `<md` Stacked cards (MVP)
    - `<DataRowCard />`: `rounded-2xl shadow-sm border p-3 space-y-2`.
    - “Zobrazit více” pro zbytek sloupců; akce v kebabu/patičce.

3) `md–lg` Scrollable table + sticky
    - `overflow-x-auto`, `min-w-*`; sticky 1–2 klíčové sloupce (left), volitelně akce (right).

4) Polishing & A11y
    - Focus ringy, `aria-label` u ikon, `aria-expanded` u “Zobrazit více”, `line-clamp`.

5) Dokumentace & usage guidelines
    - README: značení `isTitle`, `priority`, `mobileHidden`, příklady.

6) Kontrola konzistence (stavbau-ui)
    - Radius, spacing, stíny, barvy; srovnat s dalšími list komponentami.

**Akceptační kritéria:**
- Mobil bez horizontálního scrollu; čitelné karty (nezalamují layout).
- `md–lg` přirozený H-scroll + viditelné klíčové informace (sticky).
- `≥lg` beze změny.
- Přístupnost (tab stop pořadí, kontrast) a výkon (100+ řádků OK).

**Test Plan:**
- Zařízení: iPhone SE/13 Pro Max, Pixel 5/7, iPad mini, 1280/1440 px.
- Interakce: akční menu, multi-select, empty/loading/error.
- A11y: ARIA popisky, focus ringy.
- Výkon: dlouhé seznamy (chunking/virtualizace pokud zapnuta).

**Rollback:** `responsiveMode="off"` vrátí původní chování.

**Poznámky:**
- PR dělit do malých kroků (~200 LOC).
- Po každém merge přidat checkpoint do této časové osy.

## ✅ 2025-09-24 — DataTableV2 Responsive (komplet Step 1–6/6.1)

Kompletně dokončena responzivní varianta DataTableV2 (Hybrid) + zapojení do TeamPageV2.

### 🔹 Step 1/6 — API pro card fields
- Přidána module augmentation `columnDef.meta.stbMobile` pro TanStack Table.
- Typy: `isTitle`, `isSubtitle`, `priority`, `mobileHidden`, `formatter`.
- Žádná změna UI (jen příprava).

### 🔹 Step 2/6 — `<md` Stacked cards (MVP)
- Nová komponenta `<DataRowCard />` pro mobilní layout.
- Title + Subtitle + 3–5 detailů, akce vpravo.
- “Zobrazit více” pro rozbalení dalších polí.

### 🔹 Step 3/6 — `md–lg` scrollable + sticky
- Tabulka na středních breakpointech scrollovatelná (`overflow-x-auto`).
- Sticky vlevo = Title, sticky vpravo = Akce.
- ≥lg: plná tabulka, beze změny.

### 🔹 Step 4/6 — Polishing & A11y
- Přidány `aria-labelledby`, `aria-controls`, focus ringy, role="list/listitem".
- `motion-safe:animate-pulse` skeletony, `break-words` pro dlouhé texty.
- Pager propojen s tabulkou (`aria-controls`).

### 🔹 Step 5/6 — Dokumentace & usage guidelines
- Vytvořen `README.md` pro DataTableV2.
- Popsány breakpoints, metadata, props, příklady použití.
- Sekce A11y + doporučení pro vývojáře.

### 🔹 Step 6/6 — Kontrola konzistence (stavbau-ui)
- Přidán `tokens.ts` pro designové utility (`sbCardBase`, `sbDivider`, `sbFocusRing`).
- DataRowCard + DataTableV2 přepnuty na tyto utility.
- Sjednocen radius, spacing, hover, focus a barvy s ostatními komponentami.

### 🔹 Step 6/6.1 — TeamPage wired up
- `TeamPageV2` aktualizována na využití `stbMobile`.
- Mobilní karty: Title = jméno, Subtitle = e-mail, detaily = role + telefon.
- Avatar na mobilu skryt (`mobileHidden: true`).
- Desktop/střední breakpoints beze změny.

---

✅ DataTableV2 je nyní plně responzivní, konzistentní s `stavbau-ui` a nasazená v TeamPageV2.

### ✅ 2025-09-25 — TeamPageV2 – i18nNamespaces wired
- Do `TeamPageV2` doplněn prop `i18nNamespaces={['team','common']}` pro `DataTableV2`.
- Překlady mobilních karet (labely + hodnoty) nyní používají správný namespace stránky.
- Varianta připravená i pro další moduly (`invoices`, `files`, `deník`…), kde stačí předat odpovídající namespaces.

### ✅ 2025-09-26 — DataTableV2 – Responsive & Enterprise UX
- Dokončen plný **responsive hybrid režim**:
    - `<md` → karty (stacked, s přeloženými labely a actions kapslí).
    - `md–lg` → tabulka se stránkováním + filtrováním (bez hustoty).
    - `lg+` → plná tabulka s hustotou, sticky headerem a enterprise vzhledem.
- Přidány `i18nNamespaces` → překlady labelů a hodnot v kartách fungují modulárně (Team, Invoices, Files, …).
- Toolbar: mobile-first přístup (search + reset na mobilech, ostatní jen od `md`).
- Sticky header od `lg+`, blur background → lepší čitelnost při scrollu.
- `densityClasses` refaktorované: mobile-first, od `lg` kompaktnější (více řádků na obrazovku).
- Max-width container (`sbContainer`) pro `md+` → obsah vycentrovaný, na 1440/2560 nepůsobí roztahaně.
- Mobile ergonomie:
    - menší padding (`p-3` na `<sm`).
    - labely menší (`text-xs`).
    - akční tlačítka sjednocena do kapsle + min. tap target `36×36`.

---

### 🔮 FUTURE (možné vylepšení DataTableV2)
- **Column pinning / freeze** (sticky první sloupec při horizontálním scrollu).
- **Row expansion** (detail řádku rozkliknutelný přímo v tabulce).
- **Inline edit** pro vybrané sloupce.
- **Persistent user prefs** – uložit výběr sloupců, hustotu, velikost stránky do localStorage / profilu.
- **Virtualizace** (pro tisíce záznamů → výkon).
- **Skeleton loaders** – propracovanější placeholdery, které kopírují strukturu sloupců.
- **A11y enhancements** – např. voiceover-friendly labely u action buttons (už částečně hotovo).
- **Dark mode tuning** – jemné kontrasty u borderů, muted background.


## [2025-09-26] Zavedení Customer (invoices)
- ROZHODNUTO: Customer = samostatná doména v `invoices` (ne Member), dědí z BaseEntity, company-scoped.
- RBAC: invoices:read|create|update|delete na CustomersController.
- Faktury: FK `customer_id` + snapshot údajů odběratele v Invoice.
- API: /api/v1/customers (list/search, create, get, patch, delete).

TODO (MVP):
- Vxx__invoices_customers.sql (+ FK v invoices).
- Model/DTO/Mapper/Repo/Service/Web + testy.
- Napojení snapshotu v InvoiceService, OpenAPI tag.

FUTURE:
- CRM-lite „partners“ (kontakt. osoby, tagy), onboarding klienta (linkedUserId + ProjectMember role=CLIENT).
- ARES prefill zákazníků.

## [2025-09-26] Customer skeleton (invoices)
HOTOVO: Entity, Repo, DTO, Mapper, Service(+Impl), Controller s RBAC a PageResponse, bez UI.
TODO: Specifikace pro fulltext (Specifications), validační i18n messages, integrační test create→invoice snapshot.
FUTURE: Soft delete; CRM-lite (contacts, tags); ARES prefill; client portal (linkedUserId).

## [2025-09-26] Customers & RBAC – stav po integraci

### HOTOVO
- **DB migrace (Invoices/Customers)**
    - Tabulka `customers` (company-scoped), indexy (`company_id`, `ico`) a bezpečný **fulltext index** na `name`:
        - primárně `GIN (gin_trgm_ops)` při dostupném **pg_trgm**,
        - fallback `btree(lower(name))` bez rozšíření.
    - `invoices` rozšířeno o `customer_id` (FK → `customers`, **ON DELETE SET NULL**) a **snapshot** pole `buyer_*` (name/ico/dic/email/address).
    - Opraveno přidání FK (PostgreSQL neumí `ADD CONSTRAINT IF NOT EXISTS` → **DO $$** guard).
- **Common**
    - `BaseEntity` již používaná; doplněn **`CompanyScoped` (interface)** s `getCompanyId()/setCompanyId()`.
    - Přidán univerzální **`PageResponse<T>`** (Page wrapper pro REST).
- **Invoices / Customers – BE skeleton**
    - `Customer` **extends `BaseEntity` implements `CompanyScoped`**.
    - Repository (`CustomerRepository`), DTO (`CustomerDto`, `CustomerSummaryDto`, `Create*/Update*`), MapStruct `CustomerMapper`.
    - Service + Impl (`CustomerService*`) s **tenancy guardem** a základním fulltextem.
    - Controller `CustomersController`:
        - endpointy **/api/v1/customers**: list, get, create(201), patch, delete(204),
        - **RBAC**: zatím `INVOICES_*` (+ meta `INVOICES_WRITE`),
        - **OpenAPI**: `@Operation`, `@ApiResponses`, **tag `Customers`** (odděleně od Invoices).
- **RBAC – Scopes & Roles**
    - Rozšířený **katalog scopes** pro fakturaci (invoices, customers, lines, series, payments, dunning, settings, integration, VAT, reports, templates, webhooks, e-invoicing).
    - **Payments**: doplněn meta-scope `payments:write` + agregace.
    - **BuiltInRoles**: smysluplné agregace pro všechny `CompanyRoleName` (OWNER, COMPANY_ADMIN, ACCOUNTANT, …).
    - Fix: `HR_MANAGER_BASE` – `ADMIN_USERS_READ` zabalen do `of(...)` (typová korekce).
- **Aplikace startuje** (migrace OK: pg_trgm fallback + DO $$ guardy).

---

### TODO (MVP – další PRy)
- **Testy**
    - `@DataJpaTest` pro `CustomerRepository` (tenancy + fulltext).
    - `@WebMvcTest` pro `CustomersController` (200/201/204, 401/403/404/409).
    - `@SpringBootTest` integrační: `createInvoice(customerId)` → zapisuje snapshot `buyer_*` a drží `customer_id`.
- **InvoiceService**
    - Implementovat vytvoření faktury z `customerId` (prefill + snapshot), validace existence v rámci `companyId`.
- **Vyhledávání**
    - Přidat `Specification` (name/ico/dic) s `lower(...)` kompatibilní s oběma indexy (trgm/btree).
- **Validace & i18n**
    - IČO/DIČ validátory; i18n klíče (`customer.*`, konflikty typu `ico.exists`).
- **RBAC anotace & FE toggly**
    - Nechat Customers zatím na `INVOICES_*`; připravit přepnutí na `CUSTOMERS_*` (bez změny FE).
    - FE: přidat toggly pro nové scopy (payments, series, dunning…).
- **OpenAPI**
    - Zkontrolovat název security schématu (default `bearerAuth`); přidat příklady odpovědí u list/detail.

---

### FUTURE (beze zlomů veřejného API)
- **Split Customers → `customers:*`** v kontrolerech (granulárnější řízení), BE už připraveno.
- **CRM-lite „partners“**: rozšíření Customers (kontaktní osoby, více adres, tagy); zachovat `Invoice.customerId`.
- **Klientský portál**
    - Endpoint `POST /customers/{id}/link-user/{userId}` (role `CLIENT` jako `ProjectMember`).
- **Import/Export & Suggest**
    - `POST /customers/import` (CSV/XLSX/JSON), `GET /customers/export`, `GET /customers/suggest?q=`.
- **Soft delete** pro Customers (auditori, historie), politiky kolizí s FK.
- **ARES/VIES**: prefill/ověření IČO/DIČ.
- **Finance PRO**
    - Proformy, dobropisy, **recurring**, nákupní faktury, ceníky/katalog, bankovní výpisy & párování,
    - e-invoicing (ISDOC/Peppol), platební brány, VAT reporty, reporting.

---

### PR/Repo poznámky
- Dodržet **small PRs (~200 LOC)**, Conventional Commits.
- Po každém PR: aktualizovat tento soubor (sekce HOTOVO/TODO), `CHANGELOG.md`, štítky a sprint odkaz.


## [2025-09-27] [MVP] Customers – PR 1/6 (FE)
**HOTOVO**
- Základ modulu Customers (list): route `/app/customers`, debounced search (`q`), stránkování (`page`,`size`), RBAC guard (`invoices:read`).
- API klient: `listCustomers`, `getCustomer`; DTO typy včetně `PageResponse`.

**TODO (další PR)**
- Detail drawer + `getCustomer` napojení (PR 2/6).
- Create/Edit form (RHF+Zod), validace IČO/DIČ (PR 3/6).
- Delete flow s potvrzením (PR 4/6).
- i18n rozšíření + RBAC toggly pro akce (PR 5/6).
- Testy (unit/RTL/E2E) + contract check PageResponse (PR 6/6).

**FUTURE**
- Server-side sorting & advanced filtry.
- Import/Export, napojení na ARES suggest.
- Přepnutí scopes z `invoices:*` na `customers:*` pouhou změnou mapy.

## [2025-09-27] [MVP] Customers – PR 2/6 (FE)
**HOTOVO**
- Detail zákazníka jako inline drawer nad listem.
- Deep-link routa `/app/customers/:id` (sdílí stránku listu kvůli kontextu).
- `getCustomer()` + `CustomerDto`, mapování chyb (RFC7807), RBAC READ guard.

**TODO (další PR)**
- Create/Edit `CustomerForm` + validace IČO/DIČ (PR 3/6).
- Delete flow s potvrzením (PR 4/6).
- i18n doplnění textů a RBAC toggly pro akce (PR 5/6).
- Testy (unit/RTL/E2E) + contract check (PR 6/6).

**FUTURE**
- Server-side sorting + další filtry (město, IČO).
- Import/Export, ARES suggest, LinkUser.
- Přepnutí z `invoices:*` na `customers:*` jen úpravou mapy.

## [2025-09-27] [MVP] Customers – PR 3/6 (FE)
**HOTOVO**
- CustomerForm (RHF+Zod) s validací IČO/DIČ (CZ).
- CustomerFormDrawer pro create/edit, napojení na API (POST/PATCH).
- „Nový“ na listu s RBAC CREATE, route `/app/customers/new`.

**TODO (další PR)**
- Delete flow + potvrzení (PR 4/6).
- i18n doplnění tooltipů pro RBAC toggly + disable stavy (PR 5/6).
- Testy (unit: validátory, form; RTL: render & submit; E2E: create→edit) (PR 6/6).

**FUTURE**
- Validace DIČ pro další státy (EU VAT).
- ARES suggest/autofill, Import/Export.

## ✅ HOTOVO — 2025-09-27 — PR#1 FE+BE Address/Contact unifikace (MVP)
- Přidán kanonický `Address` (common/domain), `AddressDto` (common/api/dto),
  `AddressMapper` (common/mapping) a `AddressJsonConverter` (common/persistence).
- Unit test: round-trip JSON → objekt → JSON (AddressJsonConverterTest).
- Žádné změny existujících entit, žádná DB migrace.

### 🔜 TODO (PR#2)
- Refactor Customers: nahradit `billingAddressJson:String` → `Address` (JSONB) v entitě,
  DTO a mapper + migrační skript (pokud bude třeba převod legacy dat).
- Doplňkové testy: @DataJpaTest se skutečným JSONB sloupcem (Testcontainers).

### 💡 FUTURE
- Normalizační helper (např. formátování `formatted`, PSČ, house/orientation merge).
- Integrace s Geo (Mapy.cz) a ARES mappery do `Address`.
- Lokalizační labely typů adres (fakturace/dodání) pro moduly Invoices/Customers.

## ✅ HOTOVO — 2025-09-27 — PR#2 Customers → Address JSONB (typed)
- Customer: přidán `billing_address` (JSONB) + mapování na `Address`.
- DTO: `billingAddress` (AddressDto) + ponechán deprecated `billingAddressJson` pro přechod FE.
- Migrace: přidán sloupec a best-effort naplnění z legacy textu (bez dropu).
- Test: @DataJpaTest – round-trip JSONB.
- Removed entity legacy field; legacy JSON emulated in DTO mapping

### 🔜 TODO (PR#3)
- Odstranění `billingAddressJson` (sloupec + DTO) po úpravě FE.
- Doplňkové validační/normalizační helpery pro Address (PSČ, formatted).
- Integrační testy s REST (WebMvcTest) + contract test FE/BE.

### 💡 FUTURE
- Unified „address kind“ (billing/shipping/registered) + labely (i18n).
- Reuse Address pro další moduly (Projects sites, Company registered address).

### ✅ 2025-10-01 – 🟢 Modul Team – Skeleton + FE/BE integrace

## ✅ HOTOVO
- FE skeleton modulu **Team**:
    - `api/client.ts` – CRUD funkce + `updateMemberProfile`, `updateMemberRole`, `getMembersStats`
    - `api/types.ts` – sjednocené DTO (`MemberDto`, `MemberSummaryDto`, `MembersStatsDto`, requesty)
    - `components/TeamTable.tsx` – integrace s `DataTableV2`, RBAC row actions
    - `components/TeamForm.tsx` – validace přes Zod schémata, props `lockCompanyRole`, `lockReasonKey`
    - `components/TeamFormDrawer.tsx` – načítání detailu (`getMember`), integrace `useMembersStats`, `safeOnSubmit` s kontrolou posledního OWNERa
    - `components/TeamDetailDrawer.tsx` – profesionální preview člena (připraveno na rozšíření o avatar, adresy)
    - `pages/TeamPage.tsx` – integrace všech částí (list, create, edit, detail), FAB, empty states, i18n
    - `validation/schemas.ts` – `MemberSchema`, typ `AnyTeamFormValues`
- Vytvořen hook `useMembersStats` – načítá data z BE endpointu (počty členů, validace posledního OWNERa).
- Vytvořen **prompt** pro BE endpoint `GET /tenants/{companyId}/members/stats` (DTO + návrh implementace).
- UI kit: rozšířený `Button` (varianty `xs`, `fab`, decentní destructive variant).
- Upraveny empty/error/loading stavy v `TeamPage` → používají stavbau-ui a i18n.
- Refaktoring `TeamForm` a `TeamFormDrawer` – podpora uzamčení změny role, hlášky přes i18n.

## 🟡 TODO
- FE:
    - Rozšířit `TeamDetailDrawer` o profilový obrázek, trvalou a doručovací adresu.
    - Doplnit unit/integration testy pro `TeamTable`, `TeamForm`, `useMembersStats`.
    - Přidat contract testy pro `getMembersStats` (mock server).
- BE:
    - Implementovat endpoint `GET /api/v1/tenants/{companyId}/members/stats` dle připraveného promptu.
    - Pokrýt integračními testy (počty ownerů, invited, disabled, total).
- Governance:
    - Vytvořit PR: `feat(team): add members stats endpoint`.
    - Po nasazení aktualizovat i18n klíče (`errors.lastOwner`, `detail.*`).
- UX:
    - Vylepšit FAB a row actions pro mobilní zobrazení.
    - Přidat toast/notifikace po úspěšném create/edit/delete člena.

## 🕒 FUTURE
- Integrovat adresy (Registered + Delivery) do profilu člena (FE + BE).
- Podpora avatarů přes file upload (profile picture).
- Statistiky v dashboardu firmy (počty aktivních členů, invited apod. na hlavní stránce).
- Konsolidace validace mezi FE a BE (Zod ↔ Bean Validation).
- Hotový základ pro další rozšiřování profilu (CompanyMember) člena (adresy, avatar).

### ✅ 2025-10-01 – BE: Members stats endpoint (Team)
- Přidán endpoint `GET /api/v1/tenants/{companyId}/members/stats`
- RBAC: vyžaduje `team:read`
- Vrací: `{ owners, active, invited, disabled, total }` (company-scoped)
- Implementace: DTO + repo agregace (COUNT/CASE) + service + controller
- Testy: WebMvcTest (403/200), DataJpaTest (agregace)

**TODO (next):**
- Validovat/zarovnat `status` pole v `CompanyMember` (ACTIVE/INVITED/DISABLED) – sjednotit enum.
- (Volit.) cache krátkým TTL (Caffeine) pro velké firmy.
- (Volit.) rozšířit o další metriky (např. počet podle projektové role).

**FUTURE:**
- Admin náhled: stats napříč více firmami (jen pro SUPERADMIN).

### 🕒 Milník – 2025-10-01
Dokončen skeleton FE modulu **Team** (list, detail, form, drawery, RBAC, validace, i18n, hook `useMembersStats`).  
Připraven prompt pro BE endpoint `GET /members/stats`.  
Hotový základ pro další rozšiřování profilu člena (adresy, avatar).  

## 🕒 Milník 2025-10-03

### Hotovo
- Upraven `TeamForm` tak, aby podporoval `resetAfterSubmit` (výchozí true pro `create`, false pro `edit`).
- Přidán `key` na komponentu `TeamForm` (`${mode}-${memberId}`) → správný remount při změně módu nebo člena.
- Doplněn cleanup `prefill` při zavření `TeamFormDrawer` → žádná stará data při znovuotevření.
- Ošetřen lokální error nad formulářem a sjednoceno chování při submitu.
- Formulář se nyní korektně resetuje po úspěšném vytvoření člena (create), zatímco v editu zachovává hodnoty.

### TODO
- Rozšířit validace (např. phone pattern, volitelné další pole).
- Přidat loading stavy do submit tlačítka (`isLoading`) v `TeamForm`.
- Otestovat více edge-case scénářů (cancel během editace, zavření šuplíku při pending submit).

### Future
- Připravit jednotnou logiku pro validaci unikátnosti emailu už na FE (např. async validator).
- Rozšířit `TeamForm` o adresy (permanentní/doručovací) až BE endpoint bude připraven.

### ✅ 2025-10-02 – BE:  PR 1/4 – Projects: DB & model (MVP)
- Přidány tabulky: `projects`, `project_translations`, `project_members` (Flyway).
- Vytvořeny entity: Project, ProjectTranslation, ProjectMember (+ repo vrstvy).
- Přidán enum ProjectRoleName (PROJECT_MANAGER, SITE_MANAGER, QUANTITY_SURVEYOR, MEMBER, VIEWER).
- Dodržena modularita by-feature, i18n translation table, připraveno na RBAC 2.1 projektové role.
- Bez změn API (service/REST naváže v PR 2/4 a 3/4).

### ▶ TODO next
- PR 2/4: `ProjectService` + MapStruct mapper (DTO, i18n fallback, tenancy guard).
- PR 3/4: `ProjectController` + RBAC anotace (`projects:read|create|update|delete|assign`).
- PR 4/4: FE skeleton (list + create) s DataTableV2.

### ✅ 2025-10-02 – BE: PR 2/4 – doplněn i18n stack (LocaleResolver)
- Přidán LocaleResolver + LocaleContext (request-scoped), MessageService, EnumLabeler.
- Konfigurace: MessageSourceConfig, WebConfig (interceptor pro nastavení locale).
- SecurityUtils: helper currentUserLocale().
- Projects service nyní řeší fallback řetězec: ?lang → Accept-Language → user → company → app default.

### ▶ TODO next
- PR 3/4: ProjectController + @PreAuthorize + PageResponse + hlavičky `Content-Language` a `Vary: Accept-Language`.
- Přidat EnumLabeler pro `statusLabel` (Projects).
- Rozšířit list o fulltext přes `project_translations` (per-locale).

### ✅ 2025-10-03 – BE: PR 2b/4 – Company defaults (locale)
- DB: přidán sloupec `companies.default_locale` + CHECK regex; seed na `cs-CZ`.
- BE: `Company.defaultLocale` s @Pattern; repo metoda pro čtení.
- Service: `CompanyLocaleService` + impl; LocaleResolver používá firemní fallback.

### ▶ TODO next
- UI: nastavení jazyka firmy (select `cs-CZ`/`en`…), validace BCP-47.
- (volitelné) Company defaults rozšířit o `defaultCurrency`, `vatMode` (budoucí moduly).

### ✅ 2025-10-03 – BE:  PR 3/4 – Projects: REST + RBAC + i18n headers (rozpracovat)
- Controller: /api/v1/projects (list/get/create/update/delete).
- Přidán `/api/v1/projects/{id}/archive` (soft delete).
- Stubs: `POST /{id}/members`, `DELETE /{id}/members/{userId}` (zatím 202/204).
- RBAC: @PreAuthorize s 'projects:*'.
- I18n: Content-Language + Vary: Accept-Language.
- Swagger: tag "Projects".

### ▶ TODO next
- Implementovat service metody: `assignMember`, `removeMember`.
- Rozšířit list o filtry `status`, `archived`.
- @WebMvcTest testy na RBAC a i18n hlavičky.

### [2025-10-03] FE Projects skeleton
- [x] Krok 1: Založena větev `feat/projects-fe-skeleton`, přidána routa `/app/projects` s RBAC guardem (`projects:read`).
- [ ] Krok 2: API kontrakt a typy + klient s i18n hlavičkami
- [ ] Krok 3: Mappery a adaptér stránkování
- [ ] Krok 4: Skeleton komponent (Table, Form, Drawery)
- [ ] Krok 5: Hooks + RBAC toggly
- [ ] Krok 6: Stránka + vyhledávání/paging
- [ ] Krok 7: i18n + test scaffolding + PR

### [2025-10-03] FE Projects skeleton
- [x] Krok 1: Založena větev `feat/projects-fe-skeleton`, přidána routa `/app/projects` s RBAC guardem (`projects:read`).
- [x] Krok 2: Přidány `src/features/projects/api/types.ts` (DTO/enumy bez duplicit) a `src/features/projects/api/client.ts` (Axios wrapper + `Accept-Language`).
- [ ] Krok 3: Mappery a adaptér stránkování (Spring Page -> PageResponse)
- [ ] Krok 4: Skeleton komponent (Table, Form, Drawery)
- [ ] Krok 5: Hooks + RBAC toggly
- [ ] Krok 6: Stránka + vyhledávání/paging
- [ ] Krok 7: i18n + test scaffolding + PR

### [2025-10-03] FE Projects skeleton
- [x] Krok 1: Založena větev `feat/projects-fe-skeleton`, přidána routa `/app/projects` s RBAC guardem (`projects:read`).
- [x] Krok 2: Přidány `src/features/projects/api/types.ts` (DTO/enumy bez duplicit) a `src/features/projects/api/client.ts` (Axios wrapper + `Accept-Language`).
- [x] Krok 3: Vytvořen adaptér `SpringPage -> PageResponse` + normalizace `statusLabel` (ProjectsMappers.ts).
- [ ] Krok 4: Skeleton komponent (Table, Form, Drawery)
- [ ] Krok 5: Hooks + RBAC toggly
- [ ] Krok 6: Stránka + vyhledávání/paging
- [ ] Krok 7: i18n + test scaffolding + PR

### [2025-10-03] FE Projects skeleton
- [x] Krok 1: Větev + routa `/app/projects` s RBAC guardem.
- [x] Krok 2: API typy a klient (Accept-Language).
- [x] Krok 3: Adaptér Spring Page -> PageResponse + normalizace statusLabel.
- [x] Krok 4: Skeleton komponent (ProjectsTable, ProjectForm, ProjectDetailDrawer, ProjectFormDrawer) – reuse DataTableV2 & stavbau-ui.
- [ ] Krok 5: Hooks + RBAC toggly
- [ ] Krok 6: Stránka + vyhledávání/paging
- [ ] Krok 7: i18n + test scaffolding + PR

### [2025-10-03] FE Projects skeleton
- [x] Krok 5: Přidán hook `useProjectsStats` (stub – total z Page.totalElements) a `useProjectPermissions` (RBAC toggly).
- [x] Update: `ProjectsTable` – podmíněné tlačítko „Nový projekt“ přes `canCreate`.
- [ ] Krok 6: `ProjectsPage` – napojení hooků, search/paging, otevření create/detail drawerů, akce podle RBAC.
- [ ] Krok 7: i18n + test scaffolding + PR.

[2025-10-03] FE Projects skeleton
- [x] Přidán PROJECT_SCOPES (+ typ ProjectScope) – centrální konstanta pro projekty.
- [x] RBAC: zpětně kompatibilní upgrade useHasScope (umí i variantu bez userScopes – čte z auth kontextu).
- [x] useProjectPermissions aktualizován na PROJECT_SCOPES.

[2025-10-03] FE Projects skeleton
- [x] Krok 6: ProjectsPage sjednocen s TeamPage patternem (router-driven overlay, FAB, EmptyState, ScopeGuard, tokens).
- [+] Vylepšení: server-side sort (sort=name,asc), sdílené adaptace Page, připraveno na budoucí sekce (fakturace/deník).

[2025-10-03] FE Projects skeleton
- [x] Core: Přidán centrální PageResponse + toPageResponse adapter (zachovává raw Spring Page pole).
- [x] Refactor: Projects – nahrazen lokální adaptér za centrální; mappers ponechány jen pro normalizace.
- [x] ProjectsPage – přepnuto na toPageResponse (sjednocení napříč appkou).

[2025-10-04] Projects – sorting hardening
- [x] FE: odstraněn sort=name,asc (BE neumí řadit podle name).
- [x] BE: pageable() doplněn whitelist sort klíčů + fallback na code,asc; normalizace direction.
- [ ] Next: implementovat locale-aware řazení podle name (join na translations) v separátním PR.

[2025-10-04] Projects – sorting stabilized
- [x] BE: pageable() + whitelist klíčů, fallback na code,asc; opraven @PreAuthorize na archive.
- [x] FE: odstraněn problematický sort=name,asc; přidán normalizeSort whitelist (sync s BE).
- [ ] FE: přidat user-facing řazení v ProjectsTable s mapou sloupec→sort klíč (bezpečné).
- [ ] Tests: unit pro normalizeSort; E2E list → sort=status,desc.

[2025-10-04] BE – sjednocení stránkování
- [x] CustomersController.list: sjednoceno s Projects → ResponseEntity<Page<...>> + i18n headers + sort param.
- [x] Přidán pageable helper (nebo shared PageableUtils) pro konzistentní sort/page/size.
- [ ] (Nice-to-have) Sort whitelist pro Customers (name, ico, dic, ...).

## 🕒 2025-10-07 – DataTableV2 Paging Integration

### ✅ Hotovo
- Přidán `gotoPage` do `useDataTableV2Core` → plná podpora číselného stránkování.
- `setPageSize` nyní resetuje na první stránku a spouští `onPageSizeChange` + `onPageChange`.
- `TeamTable` rozšířena o řízené props:  
  `page`, `pageSize`, `total`, `onPageChange`, `onPageSizeChange`, `enableClientPaging`.
- `TeamPage` napojena na řízené stránkování (1-based logika, kompatibilní se server-side fetch).
- Opraven reset při změně `search` a `filters` (`setPage(1)` místo `setPage(0)`).
- Nový pager s naším `Button` komponentem (ikony, číselné stránky, ellipsy).

### 🎯 Výsledek
- Stabilní a konzistentní stránkování bez problikávání.
- Full sync mezi komponentami `DataTableV2`, `TeamTable`, `TeamPage`.
- Připraveno pro server-side režim (MVP ready).

### 🔮 Future
- Ukládat poslední `pageSize` do localStorage (uživatelská preference).
- Sdílený pagination context pro všechny moduly.
- Quick-jump input („Přejít na stránku…“) v pageru.  

### 🕒 7. 10. 2025 — Team & Customers: sjednocení BE/FE (MVP)

#### ✅ HOTOVO

**Backend**
- `CustomersController` sjednocen se vzorem (i18n hlavičky `Content-Language`, `Vary: Accept-Language`; paging přes `PageableUtils` s whitelistem/aliasy).
- Přidán `CustomerFilter` + `CustomerSpecification` (PR skeleton); `CustomerService.list(filter, pageable)` připraveno.
- `TeamMembersController` doplněn o i18n hlavičky, sjednocen kontrakt listu (`q/role/email/name/phone/status`, `PageResponse`).
- `MemberSummaryDto` + rozšířen `MemberMapper` (`first/last/phone` z `CompanyMember`, `role` z `member.role`).
- `PageableUtils` rozšířen o WARN logy (aliasování/fallback řazení).
- `Projects`: připraven `ProjectFilter` + `ProjectSpecification` (PR skeleton); controller/service budou řadit bezpečně podle whitelistu.

**Frontend**
- `features/team/api/types.ts` – sjednocené DTO (`MemberDto`, `MemberSummaryDto`, …).
- `features/team/api/client.ts` – používá `toPageResponse`, posílá `Accept-Language`, neposílá prázdné parametry (fix 500 na `/members`).
- `TeamTable.tsx` – integrace `DataTableV2` (email, role, jméno, telefon).
- `TeamPage.tsx` – list + search + pager, drawery (detail/form), RBAC akce (edit/role/delete).
- `features/customers/api/client.ts` – sjednoceno na `toPageResponse` + i18n hlavičky.
- `ROLE_OPTIONS` na FE napojeny na `ROLE_WHITELIST` (bez duplicit, i18n labely).

---

#### 🛠 TODO (další PR)

**Backend**
- Doplnit logiku `TeamMemberSpecification` (`q` přes `email/first/last/phone`; `role/status` filtry).
- Implementovat `CustomerSpecification` (`q` + `name/ico/dic/email`) + unit/slice testy.
- Konsolidovat sort whitelist (`Team/Customers/Projects`) + integrační testy i18n hlaviček.

**Frontend**
- `TeamPage`: server-side paging/sorting (napojit sort + řízené `page/size`).
- Centralizovat mapu `CompanyRole → i18n` (badge/labels) a doplnit testy.
- Doplnit `MSW` handlery pro `POST/PATCH/DELETE` a `RTL` testy.

---

#### 🔭 FUTURE
- `customers:*` scopy (přepnutí z `invoices:*` bez změny FE).
- **Team:** rozšířené filtry, statistiky (`owners/active/invited/disabled`) a profilová data (adresy, avatar).
- **Projects:** locale-aware řazení podle názvu (join na `translations`), advanced search.

### 2025-10-06 – Sprint 7: Zahájení sjednocení listů
- HOTOVO:
    - PR-2 (common): PageableUtils – allow-list aliasy, fallback WARN, stabilizační sort.
- TODO:
    - PR-1 (BE Projects list): q + whitelist sort + i18n hlavičky + RBAC.
    - PR-4/5/6 (FE Projects): DataTableV2 server-side + Accept-Language + RTL/MSW.
    - PR-7/8 (BE Team/Customers): sjednocení na PageableUtils + i18n hlavičky.
- FUTURE:
    - TSV fulltext per locale; společný spec builder pro filtry (status, range).

# ✅ Sprint 7 – stav k 2025-10-07

### ✔️ HOTOVO

#### Backend – Projects
- `cq.distinct(true)` kvůli `LEFT JOIN translations`.
- JOIN translations (+ preferovaný jazyk z Locale), rozšířený fulltext přes code i translations.name.
- Rozsahy dat (`plannedStartDate` / `EndDate`) + filtr `active` (archiv).
- Řazení a i18n hlavičky.
- V `ProjectController` nasazena allow-list / alias přes `PageableUtils.SortWhitelist` (`name → translations.name`), default `createdAt, desc`.
- Odpovědi posílají `Content-Language` a `Vary: Accept-Language`.

##### Typed adresa stavby
- Entita `Project`: přidáno `siteAddress (JSONB)` přes `AddressJsonConverter (@JdbcTypeCode(SqlTypes.JSON))`.
- BC mirror: `@Deprecated String siteAddressJson (read-only)` na stejný sloupec – dočasně pro kompatibilitu.
- Flyway migrace `V20251007_01__projects_site_address_json_to_jsonb.sql` – `rename projects.site_address_json → site_address`, enforce typ `jsonb`.

##### DTO / Service / Mapper
- `CreateProjectRequest` / `UpdateProjectRequest`: nové pole `siteAddress`.
- `ProjectDto`: nové pole `siteAddress`.
- `ProjectMapper` používá `AddressMapper (MapStruct)` – auto-mapování `Address ⇄ AddressDto`.
- `ProjectServiceImpl`: ukládá / patchuje `siteAddress` (typed), ostatní pole beze změny.

#### Backend – Customers
- Validace IČO (duplicit bez prázdných hodnot).
- `BusinessIdUtils.normalizeIco` (trim, odstranění mezer, prázdné → null).
- `CustomerServiceImpl.create / update`: kontrola duplicity jen pokud IČO po normalizaci existuje; jinak ukládá `NULL`.

#### Tenancy helper
- Použití `SecurityUtils.requireCompanyId()` (sjednocení opakovaného patternu).

#### Cleanup
- Doplněny Javadoc komentáře ve službě (cz verze), úklid chybových kódů (i18n keys).

---

### Frontend – UI / Forms

#### AsyncSearchSelect
- Debounce, zavírání po výběru, zavření na klik mimo i Escape, loading stav.

#### Projects – ProjectForm
- Výběr Zákazník (customers) a PM (team) přes `AsyncSearchSelect` (+ `useRequiredCompanyId()` pro team).
- Sekce **Adresa stavby (siteAddress)** přes `AddressAutocomplete` + manuální pole (formatted / street / ...).

#### Customers – CustomerForm
- Využití `AddressAutocomplete`; typed `billingAddress` (shodná struktura jako `siteAddress`).

#### Mappers (DRY)
- `features/projects/mappers.ts` a úprava `features/customers/mappers.ts`.
- Centralizace:
    - `src/lib/utils/strings.ts` → `trimToUndef`.
    - `src/lib/utils/address.ts` → `normalizeAddressDto (+ hasAnyValue)`.
- Form ⇄ API: prázdné řetězce padají na `undefined` (PATCH sémantika), adresy se jednotně normalizují.

#### API klienti (DRY)
- Nové sdílené utilitky `src/lib/api/utils.ts`:
    - `clamp`, `toInt`, `sanitizeQ`, `isCanceled`, `langHeader`, `compact`, `compactNonEmpty`, `toNonEmpty`, `normalizeSort(…, allowlist)`.
- Refaktor klientů:
    - `features/projects/api/client.ts` (vč. `createProjectFromForm / updateProjectFromForm`)
    - `features/team/api/client.ts` (allowlist sort klíčů)
    - `features/customers/api/client.ts`
- Search customers opravena na `toPageResponse` (řeší dřívější „nic nenalezeno“ při `content/items` rozdílu).

---

### Dev / ops
- Spuštění s profilem dev – opraven příkaz:
  ```bash
  mvn -Dspring-boot.run.profiles=dev spring-boot:run

### 🧩 Opravy incidentů
- **500 na `GET /api/v1/projects` (neznámý sort)** → vyřešeno *allow-list / alias* v controlleru.
- **500 na `POST /customers` při prázdném IČO** → vyřešeno `normalizeIco` + podmíněná duplicita.
- **`AsyncSearchSelect` se nezavíral** → přidán *outside-click* a *Escape handler*.

---

### 🧪 TESTY (stav)
**Připraveno / čeká na dokončení kódu:**
- `@WebMvcTest Projects`: fallback na default sort při neznámém klíči.
- `@DataJpaTest Projects`: `ProjectSpecification` (JOIN translations, q tokenizace, active / range filtry).
- **FE:** RTL + MSW pro `ProjectForm` (server-side paging / sorting + odeslání `siteAddress`).

**Dependency (navržená):**  
`org.springframework.security:spring-security-test` *(scope test)* – nutné pro `@WithMockUser`.

---

### 📌 TODO (krátkodobé)

#### PR-1 (BE Projects) – dokončit a sloučit
- Spec + controller (sort allowlist / alias + i18n hlavičky) – ✅
- Typed `siteAddress` (+ migrace) – ✅
- DTO / Mapper / Service (persist + read) – ✅
- Doplnit testy (viz výše) a limit `size ≤ 100` už v controlleru (FE už clampuje).

#### PR-4 / 5 / 6 (FE Projects) – sjednocení tabulek
- `ProjectsTable` na `DataTableV2` (server-side paging / sorting) + RTL smoke test.
- `features/projects/api/client.ts` – přepojit tabulku čistě na `listProjectSummaries`.

#### RBAC / i18n
- `EnumLabeler` pro `ProjectStatus` (+ i18n), dosadit `statusLabel` v service.
- Validovat `Content-Language` + `Vary: Accept-Language` i u Customers a Team listů (konzistence).

#### Dev data
- Seed dat pro dev (zákazníci, členové týmu, pár projektů) pro snadné QA (`skript / CommandLineRunner`).

#### Úklid BC
- Po migraci FE odstranit `Project.siteAddressJson` (read-only mirror).

---

### 🔭 FUTURE (střednědobé)
- `PageableUtils` – centralizovat allow-list a aliasy napříč moduly (Team, Customers, Projects).
- Výkon – indexy pro `projects(site_address)`, `projects(code)`, případně `translations(language,name)`; prověřit plán s JOIN translations.
- Cursor paging – příprava na `cursor` param (FE už má pole; BE zatím ignoruje).
- Soft-delete / Archiv – sjednotit delete → archive ve všech modulech.
- Audit trail – `creator / updater` na Projects (v mapperu jsou ignore, service může doplnit).

---

### 🗂️ Změněné / nové soubory (výběr)

#### Backend
- `projects/persistence/ProjectSpecification.java`
- `projects/controller/ProjectController.java`
- `projects/model/Project.java`
- `db/migration/V20251007_01__projects_site_address_json_to_jsonb.sql`
- `projects/dto/{CreateProjectRequest, UpdateProjectRequest, ProjectDto}.java`
- `projects/mapper/ProjectMapper.java`
- `projects/service/ProjectServiceImpl.java`
- `invoices/service/impl/CustomerServiceImpl.java`
- `common/util/BusinessIdUtils.java`
- `security/SecurityUtils.java`

#### Frontend
- `components/ui/stavbau-ui/AsyncSearchSelect.tsx`
- `features/projects/components/ProjectForm.tsx`
- `features/projects/mappers.ts`
- `features/customers/mappers.ts`
- `lib/utils/{strings.ts, address.ts}`
- `lib/api/utils.ts`
- `features/{projects, team, customers}/api/client.ts`
- `features/projects/api/client.ts`

---

### 🔒 Bezpečnost & zásady
- RBAC 2.1 beze změn (žádné nové scopes).
- `Accept-Language` → FE posílá; BE vrací `Vary: Accept-Language` + `Content-Language`.

---

### 📝 PR struktura (doporučení, ≤ 200 LOC / PR)
1. **PR-1 (BE / Projects):** typed `siteAddress` + DTO / Mapper / Service + migrace (malé diffy; Javadoc)
2. **PR-2 (FE / Projects):** `ProjectForm` – `AsyncSearchSelect` (customer / PM) + `siteAddress`
3. **PR-3 (FE shared):** `lib/api/utils.ts`, `lib/utils/{strings, address}.ts` + refaktor klientů
4. **PR-4 (BE / Projects):** `ProjectSpecification JOIN` + allow-list / alias + i18n hlavičky + `WebMvcTest`
5. **PR-5 (FE / Projects):** `mappers (form ⇄ API)` + `createProjectFromForm / updateProjectFromForm`
6. **PR-6 (FE / Projects):** `DataTableV2` integrace + RTL / MSW paging / sorting
7. **PR-7 (BE / Customers):** `BusinessIdUtils.normalizeIco` + cleanup + testy
8. **PR-8 (Dev):** seed pro dev profil (`CommandLineRunner`)

---

### ✅ Poznámky k rozhodnutím
- Typed `JSONB address` sjednocen mezi Customers (`billingAddress`) a Projects (`siteAddress`) – jeden konvertor, jeden DTO tvar.
- **DRY:** normalizace řetězců / adres a API utilit převedena do sdílených helperů.
- **Konzistence listů:** `PageResponse` na FE, allow-list sortů, i18n hlavičky na BE.
- Pokud někdo navrhne refaktor v těchto oblastech, **zastavit a odkázat na tento zápis** – už hotovo, držet konzistenci napříč moduly.

### ✅ PR 3a/4 – Projects: auto code generation
- DB: project_code_counters (company_id, year, value) + unique index (company_id, code).
- BE: ProjectCodeGenerator (UPSERT RETURNING), ProjectServiceImpl auto-generuje code, když není zadán.
- FE: code v create nepovinné; 409 duplicate jen při manuálním zadání.

### ▶ TODO next
- Konfigurovatelný prefix per Company (např. `company.projectCodePrefix`).
- UI: zobrazovat v detailu generovaný kód a kopírovací tlačítko.

### ✅ Projects – code immutable & autogen
- FE: code není v create/update requestech.
- BE: code se vždy generuje (R{YYYY}-{NNN}); mapper ignoruje code na update.
- DB: NOT NULL + unique (company_id, code).

### ▶ TODO next
- Konfigurovatelný formát/prefix per Company.
- Zobrazit code v UI detailu + tlačítko „kopírovat“.

### 2025-10-08 – FE: useServerTableState + fix request loop
 #### HOTOVO
- Přidán generický hook useServerTableState<T> (q/page/size/sort, load, refreshy).
- Opraven nekonečný fetch loop (stabilizovaný fetcher, deps-key, guards).
- Projects napojeny na hook; default sort code,asc.
 #### TODO
- Převést Customers/Team na useServerTableState.
- MSW+RTL smoke testy pro paging/sort + edge-case delete.
  FUTURE
- Přidat persist/restore stavu tabulek do URL (query params) a sessionStorage.

### 🕒 2025-10-08 – FE: ProjectsTable (DataTableV2Column + cleanup)
**HOTOVO**
- Přepsána komponenta `ProjectsTable` na typově bezpečný `DataTableV2Column<T>`.
- Zrušeny `as any` a kolize s `ColumnDef`.
- Sloupce plně typed + komentované (code, name, status, avatar).
- Stabilní render, `React.memo`.
  **TODO**
- Přenést stejný styl sloupců do CustomersTable a TeamTable.
- V budoucnu doplnit dynamické meta-labely pro mobilní zobrazení (stbMobile).

### 🕒 2025-10-08 – FE RBAC: univerzální useRoleOptions hook
✔️ HOTOVO
- Přidán hook useRoleOptions({ include, exclude, withAll, namespaces }) pro generování i18n role options.
- Odstraněny ad-hoc blacklisty v komponentách; UI může skrývat např. SUPERADMIN přes parametry.
- Deduplikace a fallback humanizer, i18n klíče `roles.<ROLE>`.
 použití(
   // 1) Skryjeme SUPERADMIN v běžném UI:
         const roleOptions = useRoleOptions({ exclude: ['SUPERADMIN'], withAll: true });

   // 2) Pouze několik rolí (např. pro interní dialog):
         const limitedOptions = useRoleOptions({ include: ['OWNER','COMPANY_ADMIN','VIEWER'] });

   // 3) Bez „— Vše —“ a bez filtrů:
         const allRoles = useRoleOptions();
        )

📌 TODO
- Zrevidovat existující formuláře/filtry a nahradit lokální roleOptions → useRoleOptions.
- Dopsat testy na i18n fallback a kombinace include/exclude.
- Zkontrolovat konzistenci ROLE_WHITELIST s BE katalogem (RBAC 2.1).

🔮 FUTURE
- Exponovat matrix role→scopes do admin UI (PRO fáze) + dynamický seznam rolí z BE.

# Hotovo · To-Do · Future

> Souhrn změn a plánů z tohoto vlákna. Lze vložit přímo do `hotovo-todo-future.md`.

---

## ✅ Hotovo

### 1) Unifikace service vrstvy (REST)
- Zavedeno generické **`createRestService`** a pattern „**core + wrapper(companyId)**“.
- **Team**
    - `src/features/teamV2/api/team-service.ts`
        - `core` (list/get/create/update/archive/unarchive/remove/pagedLookupFetcher)
        - `teamService(companyId)` – curried wrapper + doménové PATCHy:
            - `updateProfile(id, body)`
            - `updateRole(id, { role })`
- **Customers**
    - `src/features/customers/api/customers-service.ts`
        - Stejný pattern jako Team (list/get/create/update/remove/lookup/archive/unarchive).
- **Projects**
    - `src/features/projects/api/projects-service.ts`
        - Stejný pattern; + `archive`/`unarchive` (a případné `remove`).
- Typové opravy v `restService.ts`
    - Přidány constrainty: `CreateReq extends Record<string, any>`, `UpdateReq extends Record<string, any>` (řeší TS2344).

### 2) Stránky – přepojení na služby, sjednocení UI
- **TeamPage**
    - `src/features/team/pages/TeamPage.tsx`
    - Přepojeno na `teamService(companyId)`.
    - Sjednocené bloky: `TableHeader`, `LoadErrorStatus`, `ServerTableEmpty`, `RowActions`.
    - CRUD: `create`, `updateRole`, `updateProfile`, `remove`.
    - Filtry přes `useServerTableState` (role/status).
- **ProjectsPage**
    - `src/features/projects/pages/ProjectsPage.tsx`
    - Přepojeno na `projectsService(companyId)` (fetcher/list, CRUD/`archive`).
    - Sjednocené bloky: `TableHeader`, `LoadErrorStatus`, `ServerTableEmpty`, `RowActions`.
    - Pozn.: `ProjectsTable` používá prop **`total`** (napojeno na `total`).
- **CustomersPage**
    - `src/features/customers/pages/CustomersPage.tsx`
    - Přepojeno na `customersService(companyId)` (fetcher/list, CRUD/`archive`).
   - Sjednocené bloky: `TableHeader`, `LoadErrorStatus`, `ServerTableEmpty`, `RowActions`.
    - Doplněno `LoadErrorStatus`.

### 3) Reusable UI komponenty
- **LoadErrorStatus**
    - `src/components/ui/stavbau-ui/feedback/LoadErrorStatus.tsx`
    - ARIA `role="status"` pro loading (bez layout shiftu) + jednotný error banner s `onClear`.
- **TableHeader**
    - `src/components/ui/stavbau-ui/datatable/TableHeader.tsx`
    - `title`, `subtitle`, `actions` (pro desktop i mobil).
- **RowActions**
    - `src/components/ui/stavbau-ui/datatable/RowActions.tsx`
    - Škálovatelné akce pro řádky tabulek:
        - Inline tlačítka **+** **menu varianta** (`asMenu`, `maxInline`, `compact`).
        - Předdefinované `kind`: `detail`, `edit`, `archive`, `unarchive`, `delete` (+ custom).
        - `confirm` podpora (fallback `window.confirm`; připraveno na globální modal).
        - i18n (`i18nNamespaces`, `menuLabel`).
        - RBAC nechává na parentu (lze kombinovat se `ScopeGuard`).
    - **Použití demonstrováno** v dev stránce (viz níže).
- **ServerTableEmpty**
    - `src/components/ui/stavbau-ui/emptystate/ServerTableEmpty.tsx`
    - Jednotné empty state pro listy s vyhledáváním (`q`) i pro „no data“ scénář.

### 4) Dev dokumentace / ukázky
- **RowActionsDemo**
    - `src/pages/dev/RowActionsDemo.tsx`
    - Ukázky inline i menu varianty, potvrzování, i18n, RBAC.
    - Opravy typů: `RowAction<T>`, prop `item` (místo `row`), odstraněn neexistující `visible`.

### 5) Tabulky a formuláře
- **CustomersTable**
    - `src/features/customers/components/CustomersTable.tsx`
    - Sloupce s `id` sladěné s BE allowlistem (name/email/ico/dic/updatedAt).
    - 1-based `page` pro DataTableV2, server-side sort/pagination/search.
    - UX: ikonky (Mail/Building/IdCard), `updatedAt` formát.
- **ProjectsTable**
    - `src/features/projects/components/ProjectsTable.tsx`
- **CustomerForm**
    - `src/features/customers/components/CustomerForm.tsx`
    - Validace (zod), doplněn `email` check, collapse pro manuální adresu, GEO → `AddressDto`.
    - Fix: `orientationNumber` naplněn (kontrola mapování).
- **CustomerFormDrawer**
    - Fetch detailu na edit (prefill přes `dtoToFormDefaults`), cleanup `AbortController`, lokální error banner.
- **ProjectForm / ProjectFormDrawer**
    - Watch & set pro `customerId`, `projectManagerId` (přes `AsyncSearchSelect`).
    - GEO → `siteAddress` mapování (z `AddressSuggestion`).
    - Prefill při editu: doplnění `customerId`, `projectManagerId`, `siteAddress` (pokud je).

---

## 🧩 To-Do (další kroky)

1. **Doplnit unarchive/remove u Projects (pokud existují endpointy)**
    - `projectsService` rozšířit o `unarchive`/`remove` a přidat akce do `RowActions`.
2. **Confirm modal**
    - Nahradit `window.confirm` centralizovaným `ConfirmDialog` (UI komponenta + přístupnost).
3. **Jednotné mapování adres**
    - Ověřit `orientationNumber` vs. `houseNumber` (GEO zdroj – přesné mapování).

---

## 🚀 Future (vylepšení)

- **UI/UX**
    - Globální **ConfirmDialog** (stackovatelný, i18n, focus trap).
    - Skutečné **Dropdown/Menu** (klávesnice, roving tabindex, ARIA).
    - **Skeletony** v tabulkách (místo spinneru; bez layout shiftu).
    - **Virtualizace** tabulek pro velké dataset(y).
    - Jednotné **date/number** formátování (`Intl.*`, locale-aware).

- **Service/HTTP**
    - Volitelný **caching** (React Query / TanStack Query) + retry/backoff.
    - Telemetrie a logging (centralizované mapování API chyb).
    - Testy pro `createRestService` (unit) a Smoke/E2E pro hlavní toky.

- **State & TS**
    - Rozšířit `useServerTableState` o **cursor-based** stránkování.
    - Silnější typování **filters** (enum statusů/rolí).
    - Sdílené `RowActionKind` → mapa výchozích ikon a i18n klíčů.

- **Dev Experience**
    - Storybook stránky pro `RowActions`, `LoadErrorStatus`, `TableHeader`, `ServerTableEmpty`.
    - Dev demo: ukázky s **RBAC** kombinacemi a asynchronními potvrzeními.

---

## 🧭 Poznámky k migraci / kompatibilitě

- **ProjectsTable**: prop je `totalItems` (ne `total`). V `ProjectsPage` máme `totalItems={total}`.
- **RowActions**
    - Prop se jmenuje **`item`** (ne `row`).
    - Typ `RowAction` je **generický**: `RowAction<T>`.
    - Neexistuje prop `visible` – viditelnost řeš z parentu nebo ScopeGuardem.
    - `asMenu` přepne na menu-variant (fallback jednoduché menu; doporučené doplnit skutečný Dropdown).
- **restService.ts**
    - Typy `CreateReq` / `UpdateReq` musí splnit `Record<string, any>`.
- **Address mapping**
    - Zkontrolovat `orientationNumber` vs. `houseNumber` pro GEO provider (v Customer/Project formu).

---

## 🧪 Příklady použití (výběr)

### RowActions (menu varianta)
```tsx
<RowActions
  item={row}
  asMenu
  maxInline={2}
  i18nNamespaces={['projects','common']}
  menuLabel={t('list.actions.title', { defaultValue: 'Akce' })}
  actions={[
    { kind: 'detail', onClick: () => openDetail(row.id as UUID), scopesAnyOf: [PROJECT_SCOPES.READ] },
    { kind: 'edit', onClick: () => openEdit(row.id as UUID), scopesAnyOf: [PROJECT_SCOPES.UPDATE] },
    { kind: 'archive', onClick: () => handleArchive(row.id as UUID), scopesAnyOf: [PROJECT_SCOPES.ARCHIVE], confirm: {} },
  ]}
/>

```
Service wrapper (projects)
```ts
const projects = projectsService(companyId);

// list
await projects.list({ q, page, size, sort, filters: { status } });

// detail
const d = await projects.get(projectId);

// create / update
await projects.create(body);
await projects.update(projectId, body);

// archive / unarchive / remove
await projects.archive(projectId);
await projects.unarchive(projectId);
// await projects.remove(projectId);
```
LoadErrorStatus
```tsx
<LoadErrorStatus
  loading={loading}
  error={error}
  onClear={clearError}
  i18nNamespaces={['projects','common']}
/>
```
TableHeader
```tsx
<TableHeader
  title={t('title', { defaultValue: 'Projekty' })}
  subtitle={t('subtitle', { defaultValue: 'Správa projektů' })}
  actions={<PrimaryCtaButton onClick={openNew} />}
/>
```
### 🕒 2025-10-11 – StbEntityTable<T> (headless wrapper nad DataTableV2)
- Zavedena jednotná komponenta `StbEntityTable<T>` s 1-based stránkováním pro server-side volání.
- Props: data, page(1-based), pageSize, total, sort, search, columns, keyField, i18n/vzhled, onRowClick/rowActions/emptyContent.
- Volitelný passthrough: filters/onFiltersChange/roleOptions (kompatibilita s Team toolbar).
- Připraveno pro migraci Projects/Customers/Team wrapperů (ponechat jen columns + specifické doplňky).

# 🧭 hotovo-todo-future.md — STAVBAU-V2 (FE Overlays Unification)

## 2025-10-11 — FE: Základní orchestrátor pro drawery (CRUD) + konsolidace „single source of truth“
- Přidán **`CrudDrawer<TSummary, TFormValues>`** jako jednotný orchestrátor pro **Detail / Create / Edit** nad stávajícím drawer stackem (a11y, ESC, body-lock, focus trap řeší základní Drawer). Orchestrátor zajišťuje:
    - řízení módů (`isDetail/isNew/isEdit`), `entityId`, `onClose`
    - autoritativní načtení detailu (`fetchDetail`) + rychlý prefill ze seznamu
    - mapování detail → výchozí hodnoty formuláře (`mapDetailToFormDefaults`)
    - akce `onCreate/onEdit/onDelete` + `afterMutate`, `beforeEditSubmit`
    - sloty `renderDetail/renderCreateForm/renderEditForm` pro čistě prezentační komponenty
    - volitelný footer (default off; formuláře mají vlastní akční lištu)

  **Dopad:** stránky nyní deklarují **jeden** overlay namísto tří, načítání a chyby jsou centralizované v orchestrátoru. Zdroj dat pro detail je jednotný (autorita = `fetchDetail`). :contentReference[oaicite:0]{index=0}

---

## 2025-10-11 — FE/Team: sjednocení overlayů (Detail/Form/TeamPage)
- `components/Detail.tsx` (Team): refaktor na **čistě prezentační** komponentu (přijímá `data/loading/error` z orchestrátoru; kompat volitelný interní fetch zachován přepínačem `allowInternalFetch`). :contentReference[oaicite:1]{index=1}
- `components/Form.tsx` (Team): rozšíření o `serverError`, `onDirtyChange`, `autoFocus`, možnost zamknout editaci e-mailu v módu edit; sjednocené výchozí hodnoty a lepší a11y. :contentReference[oaicite:2]{index=2}
- `TeamPage.tsx`: nahrazení tří bloků (Detail/Create/Edit) jedním `CrudDrawer` + předávání `data/loading/error` do `Detail`. (Pozn.: v repu je ještě dočasně použit prefill; plán je přepnout na plné předávání `data/loading/error` z orchestrátoru.) :contentReference[oaicite:3]{index=3}

**Akceptační kritéria:**
- Otevření detailu i editu využívá jednotný orchestrátor (bez duplikace loaderů a error bannerů).
- Zavření overlaye a refresh listu po mutaci probíhá konzistentně napříč módy. :contentReference[oaicite:4]{index=4}

---

## 2025-10-11 — FE/Customers: sjednocení overlayů (Detail/Form/CustomersPage)
- `components/Detail.tsx` (Customers): nová **prezentační** komponenta bez fetch; UI zachováno (header akce, skeletony, confirm dialog). :contentReference[oaicite:5]{index=5}
- `components/Form.tsx` (Customers): re-export existujícího `CustomerForm` jako `Form` pro jednotné API napříč moduly (create/edit). :contentReference[oaicite:6]{index=6}
- `CustomersPage.tsx`: přechod na **jeden `CrudDrawer`** (detail/create/edit), s `fetchDetail` přes customers service a mapováním detail → form defaults. :contentReference[oaicite:7]{index=7}

**Akceptační kritéria:**
- Detail zákazníka neprovádí vlastní fetch — stav (`data/loading/error`) dodává orchestrátor.
- Create/Edit běží přes tentýž orchestrátor a po mutaci refreshují list konzistentně.

---

## 2025-10-11 — FE/Projects: sjednocení overlayů (Detail/Form/ProjectsPage)
- `components/Detail.tsx` (Projects): nová **prezentační** varianta detailu projektu (archivace/obnovení/smazání jako akce; žádný interní fetch). :contentReference[oaicite:8]{index=8}
- `components/Form.tsx` (Projects): re-export `ProjectForm` jako `Form` (sjednocené API).
- `ProjectsPage.tsx`: nahrazení `ProjectDetailDrawer` + `ProjectFormDrawer` jedním `CrudDrawer`; `fetchDetail` jako autorita; mapování detail → form defaults z původního `FormDrawer` (vč. čištění adresy a „label“ hodnot pro AsyncSelect). :contentReference[oaicite:9]{index=9}

**Pozn.:** Tlačítka a viditelnost akcí řízeny přes RBAC toggly/Scopes, konzistentně s guidelines. :contentReference[oaicite:10]{index=10}

---

## 2025-10-11 — FE/Projects: fix importu typu
- Opraven import typu `AnyProjectFormValues` v `ProjectsPage.tsx` — importovat z `../validation/schemas` (případně re-export v `components/Form.tsx` pro pohodlné importy).  
  `import type { AnyProjectFormValues } from '../validation/schemas';`

---

## Governance, struktura a pravidla (připomínka)
- Držíme **by-feature** strukturu a sjednocené konvence (DTO/Service/Controller) dle dokumentace projektu.
- PR/commity: **Conventional Commits**, malé PR, po mergi aktualizovat CHANGELOG a zápis do `hotovo-todo-future.md`. :contentReference[oaicite:12]{index=12}
- Tento zápis vznikl na základě pokynů projektu (viz `PROJECT_SETUP.md`). :contentReference[oaicite:13]{index=13}

---

## Další kroky (TODO)
- [ ] **TeamPage**: přepnout `renderDetail` z používání `prefill` na plné předávání `{data, loading, error}` z `CrudDrawer` (odstranit `allowInternalFetch` z detailu). :contentReference[oaicite:14]{index=14}
- [ ] **Customers/Projects**: odstranit staré `*DetailDrawer.tsx`/`*FormDrawer.tsx` z importů stránek (pokud jsou stále referencované), nebo je označit `@deprecated` a přesunout pod `legacy/`.
- [ ] **Unit/E2E**: doplnit testy pro orchestrátor (otevření/zavření, přepínání módů, error flow, focus trap/ESC) a smoke testy pro detail/create/edit v každém modulu.
- [ ] **Docs**: doplnit krátký README k `CrudDrawer` (API + příklady integrace) a best practices pro mapování `detail → form defaults`.

---
