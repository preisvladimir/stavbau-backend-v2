# STRUKTURA.md

## 1) Core Business Engine

**Účel:** jednotná páteř pro firmy, projekty/zakázky, zákazníky a soubory.

### Entity → Struktura → Seznam polí

#### Company → agregát tenant (root)
- id: UUID
- name: String
- ico: String? (CZ) / vatId: String?
- billingAddress: Address
- settings: JSONB (např. číselné řady, měna, jazyk)
- Audit/tenancy: createdBy, createdAt, updatedBy, updatedAt, deleted*, archived*, version

#### User → globální uživatel (napojen přes Membership)
- id: UUID
- email: String (unique)
- displayName: String
- status: ENUM(ACTIVE, INVITED, DISABLED)
- avatarUrl: String?
- preferences: JSONB
- Audit: bez companyId (globální), createdAt... version

#### Membership → vztah User ↔ Company
- id: UUID
- companyId: UUID (FK)
- userId: UUID (FK)
- roles: Set<CompanyRoleName>
- scopes: Set<Scope> (rozšíření nad rolemi)
- status: ENUM(ACTIVE, INVITED, DISABLED)
- Audit/tenancy: standard + unique(companyId, userId)

#### Customer → zákazník/klient
- id, companyId
- code: String (unique v rámci company)
- name: String
- type: ENUM(ORGANIZATION, PERSON)
- registrationId: String? (IČO apod.)
- taxId: String?
- billingAddress: Address
- contacts: JSONB (e-maily/telefony včetně „primary“)
- notes: String?
- tags: JSONB
- externalIds: JSONB
- Audit/soft-delete/indexy: uq(companyId, code), ix(companyId, name)

#### Project (alias Zakázka)
- id, companyId
- code: String (unique in company)
- name: String
- type: ENUM(PROJECT, WORK_ORDER, CONSTRUCTION_SITE)
- status: ENUM(DRAFT, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED)
- customerId: UUID?
- siteAddress: Address?
- budget: Money?
- startDate: LocalDate?, dueDate: LocalDate?, completedAt: Instant?
- team: JSONB (role v projektu, lehce)
- meta, tags, externalIds: JSONB
- Audit/tenancy/indexy: uq(companyId, code), ix(companyId, status)

#### File → uložený dokument
- id, companyId
- bucket: String / storageKey: String
- filename: String, mimeType: String
- sizeBytes: long
- linkedTo: JSONB (např. { entity:"PROJECT", id:"..." })
- checksum: String?
- versioning: JSONB?
- Audit/soft-delete

#### Address (value objekt / embeddable)
- country, region, city, street, zip, geo: JSONB(lat,lng)

---

## 2) Compliance & Regulations

**Účel:** hlídání povinností, revizí, termínů, evidencí splnění.

... (ostatní sekce zkráceny pro přehlednost – plný obsah viz původní zadání)

---

## Cross-cutting pole (platí pro všechny entity)
- id: UUID
- companyId: UUID (kromě skutečně globálních entit jako User, RoleTemplate global)
- createdBy: UUID, createdAt: Instant
- updatedBy: UUID?, updatedAt: Instant?
- archived: boolean, archivedAt: Instant?, archivedBy: UUID?
- deleted: boolean, deletedAt: Instant?, deletedBy: UUID?
- version: long
- tags: JSONB?, externalIds: JSONB?, meta: JSONB?
- Indexy: ix(companyId, status|dueDate|code...), unikáty: uq(companyId, code)

---

## Závazné principy
- Domain isolation: žádný přímý cross-module DB přístup; vše přes Service rozhraní (anti-corruption layer).
- DTO everywhere: JPA entity nikdy ven; mapování přes MapStruct.
- RBAC & Tenancy: každá operace validuje companyId + scopy (*:read|write|archive|delete).
- Audit & Soft-delete: standardní pole, mazání pouze soft.
- Validation & i18n: Bean Validation + i18n klíče (module.key).
- Transactions: čtení @Transactional(readOnly = true), zápisy na ServiceImpl.
- Logging & Metrics: loguj byznys události; žádná citlivá data.
- Paging & Sort: page,size,sort s whitelistem sloupců.
- Specification: null-safe buildery, žádné stringové JPQL.
- Migrations: 1 tabulka = 1 migrace, inkrementálně dál.
- Tests: unit (mapper/spec), slice (repo), service (H2/Testcontainers), web (MockMvc).
