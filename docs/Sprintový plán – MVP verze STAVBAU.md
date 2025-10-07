## 🗓️ Sprintový plán – MVP verze STAVBAU (aktualizace k 2025-10-07)

Týdenní sprinty pro vývoj funkčního **MVP (verze 1.0)**  
Cílem je do konce roku 2025 uzavřít funkční minimum pro pilotní testování s reálnými firmami.

Každý sprint má jasně vymezené cíle, prioritní úkoly a výstupy.  
Rozšíření uvedená v jednotlivých sprintech reflektují aktuální stav vývoje a známé požadavky z „hotovo-todo-future.md“.

---

### 🚀 Sprint 1 – Inicializace projektu ✅ Hotovo

* Vytvoření repozitářů (BE + FE)
* Konfigurace `pom.xml`, PostgreSQL, Docker Compose
* `StavbauBackendApplication.java` + HelloWorld endpoint
* CI/CD pipeline, základní struktura monolitu (by feature)

**Výstup:** Aplikace spustitelná lokálně, DB připojení + CI build zelený

---

### 🔐 Sprint 2 – Autentizace a uživatelé ✅ Hotovo

* Entita `User` + `Role` enum
* `AuthController`, `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`
* RBAC 2.1 (MVP) – company/project roles + scopes
* Endpoint `/auth/me` + JWT payload se scopes
* Základní i18n error messages

**Výstup:** JWT autentizace, login/refresh, RBAC základ

---

### 🌍 Sprint 3 – Integrace základních služeb (Geo / Weather / ARES) ⚙️ Částečně hotovo

* **ARES** lookup firmy → Company prefill ✅
* **Geo API** (Mapy.cz suggest/reverse) skeleton
* **Weather (Meteostat)** – připraven client + fallback provider
* Scopes: `geo:read`, `weather:read`, `ares:read`
* FE: autocomplete adres

**Rozšíření (Q4/2025):**  
Integrovat `AddressAutocomplete` (již ve FE) + plné cacheování Geo výsledků

**Výstup:** Integrované služby ARES + adresy (Geo a Weather v rozpracování)

---

### 💰 Sprint 4 – Finance a dokumentace (Invoices & Files) ⚙️ Základ připraven

* Entity `Invoice`, `InvoiceLine`, `NumberSeries`, `Customer`
* `InvoiceService` + `PDF export` skeleton
* `FilesController` + lokální úložiště (`LocalFsStorage`)
* Propojení Invoices ↔ ARES (připraveno v service)
* Scopes: `invoices:*`, `files:*`

**Rozšíření (Q4/2025):**
* PDF šablony + i18n exporty
* Realtime notifikace na nový soubor/fakturu (SSE)
* FE Files Page – upload + tagging

**Výstup:** Funkční Invoices/Files s napojením na ARES – rozšíření během Sprintu 9

---

### 🔔 Sprint 5 – Notifikace a AI Skeleton ⏳ Nezahájeno

* Implementace SSE (`notifications` tabulka, server events)
* FE hook `useNotifications()` + navbar badge
* AI modul (skeleton) – analýza obrázků (OOPP detekce)
* Offline cache (PWA skeleton) pro deník

**Rozšíření (Q1/2026):**
* Integrace AI engine + fotky v deníku

**Výstup:** Připravené realtime a AI základy

---

### 🏗️ Sprint 6 – Projekty a členové ✅ Hotovo

* Entity `Project`, `ProjectMember`
* CRUD pro projekty a tým + RBAC kontrola
* TeamMembersController + i18n hlavičky
* `PageResponse` DTO sjednoceno

**Výstup:** Správa projektů a týmu hotová (MVP úroveň)

---

### 📒 Sprint 7 – Paging / Sorting / i18n sjednocení ✅ Probíhá (Q4 2025)

* Dokončení `ProjectSpecification` (JOIN translations + q tokenizace)
* Allow-list sortů (`PageableUtils.SortWhitelist`) + aliasy
* Typed `siteAddress` (typed JSONB + Flyway migrace)
* `CustomerServiceImpl` – normalize IČO, tenancy helper
* FE `DataTableV2` – ProjectsTable server-side paging/sorting
* FE AsyncSearchSelect – bugfix (outside click + escape)

**Výstup:** Konzistentní paging/sorting napříč Team / Customers / Projects + typed adresy

---

### ✅ Sprint 8 – Rozpočet (Budget) a filtrace ⚙️ Připravuje se

* Entita `BudgetItem` + `BudgetService` + DTO/Mapper
* FE `BudgetPage` + DataTableV2 s kategoriemi
* Server-side filtrace (Company/Project)
* Rozšíření `TeamMemberSpecification` a `CustomerSpecification`
* Testy: slice testy + RTL pro BudgetForm

**Výstup:** Funkční rozpočet napojený na projekt + sjednocený filter stack

---

### 📦 Sprint 9 – Files / Invoices / Notifications

* FE `FilesPage` – upload/tagging/list
* Invoices – PDF export + i18n fallbacky
* SSE notifikace pro nové soubory/faktury
* RBAC kontrola přístupů k souborům

**Rozšíření:**
* Files – preview + metadata
* Invoices – e-mail odesílání (s i18n šablonou)

**Výstup:** Plně funkční Files a Invoices moduly + notifikace

---

### 📘 Sprint 10 – Deník a Úkoly

* Entity `LogEntry`, `Task`
* Weather API integrace do deníku
* CRUD zápisy v deníku (+ fotek)
* FE Tasks modul – filtrování podle projektu a uživatele
* Hook `useNotifications()` integrovaný do FE

**Výstup:** Deník a To-Do modul propojený s projekty

---

### 🧪 Sprint 11 – Stabilizace a testování (Q4 → Q1 2026)

* Testy: `@DataJpaTest`, `@WebMvcTest`, `RTL` coverage ≥ 80 %
* Kontrola RBAC a i18n na všech endpoint listů
* API dokumentace (OpenAPI / Swagger)
* Pilotní dataset (CommandLineRunner – seed 3–5 firem)
* Deploy na testovací instanci + feedback cyklus

**Výstup:** MVP připravené pro pilotní nasazení a sběr zpětné vazby

---

## 🔭 Post-MVP (Q1 – Q3 2026)

| Oblast | Cíl / Rozšíření |
|:--|:--|
| **RBAC PRO verze** | Role v DB, FE editor oprávnění, cache invalidace |
| **Marketplace modul** | Poptávky ↔ řemeslníci, geo-mapa, cenové nabídky |
| **AI Asistent** | Generování rozpočtu, klasifikace fotek, chat-analýza |
| **PWA / mobilní verze** | Offline sync, instalovatelná appka |
| **Reporting & Analytics** | Přehledy projektů, rozpočtů, časové řady výdajů |
| **Integrace PRODOMA API** | Import materiálů / cen / dodavatelů |

---

📌 **Stav k 7. říjnu 2025:**
- Sprinty 1 → 7 uzavřené nebo v běhu.
- Sprint 8 → 10 navazují na sjednocené filtrační a i18n infrastruktury.
- Sprint 11 bude zaměřen na stabilizaci a přípravu MVP pro pilotní firmy.  
