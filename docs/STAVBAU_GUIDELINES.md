# 📑 STAVBAU_GUIDELINES.md

## 1. Základní pravidlo
Vždy se při práci opírat o nahrané referenční dokumenty, nepsat nic, co by bylo v rozporu s těmito zdroji. Pokud nastane konflikt, upozornit na něj a doporučit řešení (sloučení, oprava, doplnění).

---

## 2. Dokumenty a jejich účel
- **`bussines plan.md`**  
  🔹 Hlavní referenční dokument pro business plán, cílové skupiny, monetizaci, konkurenci a strategii rozvoje.  
  ➝ Při tvorbě textů, prezentací, pitch decků, cenových modelů a marketingových úvah se vždy držet tohoto dokumentu.

- **`Sprintový plán – MVP verze STAVBAU.md`**  
  🔹 Rozpad práce do sprintů, definice kroků a priorit pro MVP.  
  ➝ Každý nový task, návrh nebo diskuze o implementaci musí být navázána na tento sprintový plán.  
  ➝ Pokud se řeší úkol mimo plán, doporučit jeho zařazení do vhodného sprintu.

- **`struktury projektu (balíčky & vrstvy) - včetně i18n`** a **`modular monolith (by feature).md`**  
  🔹 Referenční dokumenty pro architekturu backendu (Spring Boot, modular monolith, DDD by feature, i18n).  
  ➝ Každý nový kód, návrh entity, API nebo FE struktury musí být konzistentní s těmito strukturami.  
  ➝ Pokud něco chybí, doporučit rozšíření dokumentu, ne tvořit ad-hoc řešení.

- **`hotovo-todo-future.md`**  
  🔹 Jediný zdroj pravdy o tom, co už je hotovo, co je TODO a co je v plánu (future).  
  ➝ Nikdy nenavrhovat refaktorování něčeho, co je už hotové, pokud není důvod (např. bug, změna požadavků).  
  ➝ Každý dokončený úkol ihned zaznamenat (shrnutí v chatu + doplnění do souboru).  
  ➝ Pokud navrhnu něco, co už bylo hotové, upozorni mě a odkaž se na tento dokument.

---

## 3. Styl práce
1. **Navazovat na existující zdroje** – vždy se odkazuj na konkrétní dokument, odkud čerpáš.  
2. **Doplňovat časovou osu** – pokud se něco udělá, hned vytvořit checkpoint do `hotovo-todo-future.md`.  
3. **Minimalizovat rework** – vždy hlídat, abychom nepřepisovali hotové části bez vážného důvodu.  
4. **Jasně rozlišovat roviny** – business (plán), sprint (MVP kroky), architektura (struktury, modular monolith), stav (hotovo-todo-future).  

---

## 4. Priority
1. **Business plan** (kam směřujeme)  
2. **Sprintový plán (MVP)** (co právě děláme)  
3. **Struktura projektu** (jak to děláme technicky)  
4. **Hotovo-TODO-Future** (co už je a co chybí)  

---

## 5. Chování a mindset
1. **Programovat profesionálně**  
   - Čistý, udržovatelný kód, plně dokumentovaný (JavaDoc, komentáře, README).  
   - Architektura navržená tak, aby byla rozšiřitelná bez nutnosti zásadních refaktorů.  
   - Dodržovat best practices (DDD, SOLID, REST API standardy, bezpečnostní zásady).  

2. **Myslet na budoucnost**  
   - Každé rozhodnutí hodnotit i z pohledu škálovatelnosti a dlouhodobého provozu.  
   - Vyhýbat se „rychlým hackům“, pokud by mohly v budoucnu znamenat drahé opravy.  
   - Vždy zohlednit modularitu a možnost pozdější migrace na microservices, pokud by to dávalo smysl.  

3. **Průběžná analýza trhu**  
   - Pravidelně reflektovat, zda směr vývoje odpovídá trendům ve stavebnictví, SaaS a B2B nástrojích.  
   - Identifikovat příležitosti pro MVP i PRO verzi (nové funkce, integrace, AI, legislativní požadavky).  
   - Pokud se objeví signál, že určitý modul nebo směr vývoje nemá budoucnost → upozornit a navrhnout alternativu.  

4. **Zodpovědnost za kvalitu**  
   - Každý nový kód/část systému musí zapadnout do již definovaných struktur a pravidel.  
   - Hlídám, aby se neprogramovalo něco, co už je hotové (viz `hotovo-todo-future.md`).  
   - Každý krok vysvětlit tak, aby byl jasný i při čtení s odstupem času.  

5. **Workflow & připomínky (commity, checkpointy, návaznost)**  
   - **Commity a push**: po **každé uzavřené jednotce práce** (endpoint, entita + migrace, služba + testy, FE stránka/komponenta) _nebo_ minimálně po **45–60 minutách**:  
     - commit ve stylu **Conventional Commits** (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`, `build:`, `ci:`), krátký imperativ, co se změnilo a proč.  
     - malé PR > rychlé review; velká PR dělit.  
   - **Automatické checkpointy**: po dokončení bloku práce **vytvořit záznam** do `hotovo-todo-future.md` (sekce HOTOVO) a případně doplnit TODO pro návazný krok. Záznam musí obsahovat:  
     - datum/čas, _rozsah změn_, _dotčené moduly (BE/FE/DB/i18n/security)_, _důvod_ a _dopad_.  
   - **Nepsat kód „dopředu“**: neimplementovat rozsáhlé funkce nad rámec domluveného kroku.  
     - vždy navrhnout **minimální další krok** (MVP inkrement), u větších témat přiložit 2–3 varianty s trade‑offs a doporučení.  
     - pokud jsou nutné předpoklady, **explicitně je vypsat** (sekce „Assumptions“) a držet se nejbezpečnější varianty.  
   - **Důkladná analýza dalšího kroku** (dependency‑aware): před implementací stručně formulovat **Step Plan**:  
     - Cíl, Vstupy/závislosti, Změněné části (BE/FE/DB), Migrace, Bezpečnost & i18n dopady, Akceptační kritéria, Test plan, Rollback.  
   - **Připomínky**: pokud blok práce přesáhne doporučené okno nebo se mění >5 souborů / >200 řádků, upozorním na nutnost **commit/push + checkpoint**.  
   - **Aktualizace sprintu**: pokud změna rozšíří/zúží scope, **aktualizovat `Sprintový plán – MVP verze STAVBAU.md`** (poznámka v daném sprintu).  

---



---

## 6. Použití šablon
- Pro každý commit a větší krok v projektu používej šablony z **`STAVBAU_TEMPLATES.md`**.  
- Commit message musí následovat konvenci (Conventional Commits).  
- Každý větší krok začni analýzou pomocí **Step Plan** šablony.  
