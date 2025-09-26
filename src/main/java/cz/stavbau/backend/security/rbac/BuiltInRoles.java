package cz.stavbau.backend.security.rbac;

import java.util.*;

import static cz.stavbau.backend.security.rbac.Scopes.*;
import static java.util.Map.entry;

/**
 * MVP in-code role→scopes (RBAC 2.1 §5.1, §3.1/3.2).
 * PRO fáze: perzistence v DB (RBAC 2.1 §5.2).
 */
public final class BuiltInRoles {

    private BuiltInRoles() {}

    /* ---------------------------
     * Helpery pro skládání setů
     * --------------------------- */
    @SafeVarargs
    private static Set<String> union(Set<String>... sets) {
        Set<String> out = new HashSet<>();
        for (Set<String> s : sets) out.addAll(s);
        return Collections.unmodifiableSet(out);
    }
    private static Set<String> minus(Set<String> base, Set<String> remove) {
        Set<String> out = new HashSet<>(base);
        out.removeAll(remove);
        return Collections.unmodifiableSet(out);
    }
    private static Set<String> of(String... scopes) { return Set.of(scopes); }

    /* ---------------------------
     * Agregace pro Team & Projects
     * --------------------------- */
    private static final Set<String> TEAM_R  = of(TEAM_READ);
    // TEAM_WRITE: meta-scope + rozpad na jemné scopy kvůli @PreAuthorize hasAnyScope(...)
    private static final Set<String> TEAM_RW = union(
            of(TEAM_WRITE), // meta
            of(TEAM_READ, TEAM_ADD, TEAM_REMOVE, TEAM_UPDATE, TEAM_UPDATE_ROLE)
    );
    private static final Set<String> PROJECTS_RW = of(
            PROJECTS_READ, PROJECTS_CREATE, PROJECTS_UPDATE, PROJECTS_DELETE, PROJECTS_ARCHIVE, PROJECTS_ASSIGN
    );

    /* ---------------------------
     * Agregace pro Files/Logs/Budget
     * --------------------------- */
    private static final Set<String> FILES_RW = of(
            FILES_READ, FILES_UPLOAD, FILES_UPDATE, FILES_DELETE, FILES_DOWNLOAD, FILES_SHARE
    );
    private static final Set<String> LOGBOOK_RW = of(
            LOGBOOK_READ, LOGBOOK_CREATE, LOGBOOK_UPDATE, LOGBOOK_DELETE, LOGBOOK_EXPORT
    );
    private static final Set<String> BUDGET_RW = of(
            BUDGET_READ, BUDGET_CREATE, BUDGET_UPDATE, BUDGET_DELETE, BUDGET_EXPORT
    );
    private static final Set<String> BUDGET_APPROVAL = of(BUDGET_APPROVE);

    /* ---------------------------
     * Invoices – core a související
     * --------------------------- */
    // INVOICES_WRITE: meta-scope + plný rozpad na jemné scopy
    private static final Set<String> INVOICES_CORE_FULL = union(
            of(INVOICES_WRITE), // meta
            of(INVOICES_READ, INVOICES_CREATE, INVOICES_UPDATE, INVOICES_DELETE,
                    INVOICES_EXPORT, INVOICES_APPROVE, INVOICES_ISSUE, INVOICES_CANCEL,
                    INVOICES_DUPLICATE, INVOICES_CONVERT_PROF, INVOICES_CREATE_CN,
                    INVOICES_MARK_PAID, INVOICES_UNMARK_PAID, INVOICES_CHANGE_DUE,
                    INVOICES_LOCK, INVOICES_PDF, INVOICES_EMAIL_PREVIEW, INVOICES_EMAIL_SEND)
    );
    private static final Set<String> INVOICES_CORE_RW = of(
            INVOICES_READ, INVOICES_CREATE, INVOICES_UPDATE, INVOICES_DELETE, INVOICES_EXPORT,
            INVOICES_PDF, INVOICES_EMAIL_PREVIEW
    );
    private static final Set<String> INVOICE_LINES_RW = of(
            INVOICE_LINES_READ, INVOICE_LINES_CREATE, INVOICE_LINES_UPDATE,
            INVOICE_LINES_DELETE, INVOICE_LINES_REORDER, INVOICE_LINES_DISCOUNT, INVOICE_LINES_TAX_OVR
    );
    private static final Set<String> CUSTOMERS_RW = of(
            CUSTOMERS_READ, CUSTOMERS_CREATE, CUSTOMERS_UPDATE, CUSTOMERS_DELETE,
            CUSTOMERS_IMPORT, CUSTOMERS_EXPORT, CUSTOMERS_LINK_USER
    );
    private static final Set<String> SERIES_ADMIN = of(
            SERIES_READ, SERIES_CONFIGURE, SERIES_RESERVE, SERIES_RELEASE, SERIES_RESET,
            SERIES_IMPORT, SERIES_EXPORT
    );
    private static final Set<String> PAYMENTS_R  = Set.of(PAYMENTS_READ, PAYMENTS_EXPORT);
    private static final Set<String> PAYMENTS_RW = Set.of(
            PAYMENTS_WRITE,
            PAYMENTS_READ, PAYMENTS_RECORD, PAYMENTS_UPDATE, PAYMENTS_DELETE,
            PAYMENTS_RECONCILE, PAYMENTS_WRITEOFF, PAYMENTS_REFUND, PAYMENTS_EXPORT
    );
    private static final Set<String> DUNNING_ADMIN = of(
            DUNNING_READ, DUNNING_CONFIGURE, DUNNING_SEND, DUNNING_SCHEDULE, DUNNING_CANCEL
    );
    private static final Set<String> INV_SETTINGS_ADMIN = of(
            INV_SETTINGS_READ, INV_SETTINGS_UPDATE
    );
    private static final Set<String> INV_INTEGRATION_ADMIN = of(
            INV_INTEGRATION_CONN, INV_INTEGRATION_SYNC, INV_INTEGRATION_EXPORT, INV_INTEGRATION_IMPORT
    );
    private static final Set<String> INV_I18N_RW = of(
            INVOICES_I18N_READ, INVOICES_I18N_UPDATE
    );
    private static final Set<String> VAT_RW = of(
            INV_VAT_REPORT, INV_VAT_EXPORT, VAT_LEDGER_READ, VAT_LEDGER_EXPORT
    );
    private static final Set<String> REPORTS_INVOICES_R = of(
            REPORTS_INVOICES_READ, REPORTS_INVOICES_EXPORT
    );
    private static final Set<String> TEMPLATES_RW = of(
            TEMPLATES_READ, TEMPLATES_UPDATE, TEMPLATES_IMPORT, TEMPLATES_EXPORT
    );
    private static final Set<String> WEBHOOKS_RW = of(
            WEBHOOKS_READ, WEBHOOKS_CREATE, WEBHOOKS_DELETE, WEBHOOKS_TEST
    );
    private static final Set<String> E_INVOICING_RW = of(
            E_INVOICING_SEND, E_INVOICING_VERIFY, E_INVOICING_SETTINGS
    );

    /* ---------------------------
     * PRO rozšíření (finance/obchod)
     * --------------------------- */
    private static final Set<String> PROFORMAS_ALL = of(
            PROFORMAS_READ, PROFORMAS_CREATE, PROFORMAS_UPDATE, PROFORMAS_DELETE,
            PROFORMAS_ISSUE, PROFORMAS_CANCEL, PROFORMAS_EXPORT
    );
    private static final Set<String> CREDIT_NOTES_ALL = of(
            CREDIT_NOTES_READ, CREDIT_NOTES_CREATE, CREDIT_NOTES_UPDATE, CREDIT_NOTES_DELETE,
            CREDIT_NOTES_ISSUE, CREDIT_NOTES_CANCEL, CREDIT_NOTES_EXPORT
    );
    private static final Set<String> RECURRING_ALL = of(
            RECURRING_INVOICES_READ, RECURRING_INVOICES_CREATE, RECURRING_INVOICES_UPDATE,
            RECURRING_INVOICES_DELETE, RECURRING_INVOICES_RUN, RECURRING_INVOICES_PAUSE,
            RECURRING_INVOICES_RESUME, RECURRING_INVOICES_PREVIEW
    );
    private static final Set<String> PURCHASE_INV_ALL = of(
            PURCHASE_INVOICES_READ, PURCHASE_INVOICES_CREATE, PURCHASE_INVOICES_UPDATE,
            PURCHASE_INVOICES_DELETE, PURCHASE_INVOICES_APPROVE,
            PURCHASE_INVOICES_MARK_PAID, PURCHASE_INVOICES_UNMARK_PAID,
            PURCHASE_INVOICES_EXPORT
    );
    private static final Set<String> CATALOG_RW = of(
            CATALOG_READ, CATALOG_CREATE, CATALOG_UPDATE, CATALOG_DELETE, CATALOG_IMPORT, CATALOG_EXPORT
    );
    private static final Set<String> PRICE_LISTS_RW = of(
            PRICE_LISTS_READ, PRICE_LISTS_CREATE, PRICE_LISTS_UPDATE, PRICE_LISTS_DELETE, PRICE_LISTS_ASSIGN
    );
    private static final Set<String> TAXES_UNITS_CURR_RW = of(
            TAXES_READ, TAXES_UPDATE, UNITS_READ, UNITS_UPDATE, CURRENCIES_READ, CURRENCIES_UPDATE,
            FX_RATES_READ, FX_RATES_SYNC
    );
    private static final Set<String> BANKING_RW = of(
            BANK_ACCOUNTS_READ, BANK_ACCOUNTS_MANAGE,
            BANK_STATEMENTS_IMPORT, BANK_STATEMENTS_DELETE,
            BANK_RECONCILIATION_RUN, BANK_RECONCILIATION_UNDO
    );

    /* ---------------------------
     * Admin / Integrations (globální)
     * --------------------------- */
    private static final Set<String> ADMIN_RW = of(ADMIN_USERS_READ, ADMIN_USERS_MANAGE, INTEGRATIONS_MANAGE);

    /* ---------------------------
     * Balíčky pro role
     * --------------------------- */

    // Superadmin – vše (platformní bypass). Zachováme i meta-scopes.
    private static final Set<String> SUPERADMIN_ALL = union(
            of(DASHBOARD_VIEW),
            PROJECTS_RW, TEAM_RW, FILES_RW, LOGBOOK_RW, union(BUDGET_RW, BUDGET_APPROVAL),
            ADMIN_RW,

            // Invoices & finance + PRO rozšíření
            INVOICES_CORE_FULL, CUSTOMERS_RW, INVOICE_LINES_RW, SERIES_ADMIN, PAYMENTS_RW,
            DUNNING_ADMIN, INV_SETTINGS_ADMIN, INV_INTEGRATION_ADMIN, INV_I18N_RW,
            VAT_RW, REPORTS_INVOICES_R, TEMPLATES_RW, WEBHOOKS_RW, E_INVOICING_RW,
            PROFORMAS_ALL, CREDIT_NOTES_ALL, RECURRING_ALL, PURCHASE_INV_ALL,
            CATALOG_RW, PRICE_LISTS_RW, TAXES_UNITS_CURR_RW, BANKING_RW
    );

    // Owner – “vše pro firmu” (stejné jako superadmin, ale bez platformních výjimek)
    private static final Set<String> OWNER_ALL = SUPERADMIN_ALL;

    // Company admin – široká správa firmy, bez některých rizikových/účetních operací
    // Odebereme např. INVOICES_LOCK a ADMIN_USERS_MANAGE, BUDGET_APPROVE (politika firmy).
    private static final Set<String> COMPANY_ADMIN_BASE = minus(
            union(OWNER_ALL),
            union(
                    of(INVOICES_LOCK, ADMIN_USERS_MANAGE, BUDGET_APPROVE)
            )
    );

    // Accountant – účetní agenda napříč fakturací, bankou a výkazy
    private static final Set<String> ACCOUNTANT_BASE = union(
            of(DASHBOARD_VIEW),
            PAYMENTS_RW, INVOICES_CORE_FULL, CUSTOMERS_RW, INVOICE_LINES_RW, SERIES_ADMIN,
            PAYMENTS_RW, DUNNING_ADMIN, INV_SETTINGS_ADMIN, INV_INTEGRATION_ADMIN,
            VAT_RW, REPORTS_INVOICES_R, TEMPLATES_RW, E_INVOICING_RW, BANKING_RW,
            FILES_RW // účtárna typicky pracuje se soubory
    );

    // Purchasing – nákupní doklady + katalogy/ceníky
    private static final Set<String> PURCHASING_BASE = union(
            of(DASHBOARD_VIEW),
            PURCHASE_INV_ALL, CATALOG_RW, PRICE_LISTS_RW, CUSTOMERS_RW, // dodavatelé mohou žít v customers
            FILES_RW, PROJECTS_RW
    );

    // Manager – řízení projektů, práce s dokumenty, omezená fakturace (bez finálního vydání/platby)
    private static final Set<String> MANAGER_BASE = union(
            of(DASHBOARD_VIEW),
            PROJECTS_RW, LOGBOOK_RW, FILES_RW, BUDGET_RW,
            CUSTOMERS_RW,
            // Omezená práce s fakturami (bez issue/cancel/mark_paid/lock)
            INVOICES_CORE_RW, INVOICE_LINES_RW, SERIES_ADMIN
    );

    // Document Controller – správa dokumentů/šablon, podpora e-mailů/PDF, read-only finance
    private static final Set<String> DOC_CONTROLLER_BASE = union(
            of(DASHBOARD_VIEW),
            FILES_RW, TEMPLATES_RW,
            of(INVOICES_READ, INVOICES_PDF, INVOICES_EMAIL_PREVIEW, INVOICES_EXPORT),
            CUSTOMERS_RW
    );

    // Fleet Manager – minimální práva (dle doménového rozsahu mimo finance)
    private static final Set<String> FLEET_MANAGER_BASE = union(
            of(DASHBOARD_VIEW),
            FILES_RW, PROJECTS_RW
    );

    // HR Manager – týmová administrativa + přístup k projektům/files (bez financí)
    private static final Set<String> HR_MANAGER_BASE = union(
            of(DASHBOARD_VIEW),
            TEAM_RW, PROJECTS_RW, FILES_RW, of(ADMIN_USERS_READ)
    );

    // Auditor – read-only napříč moduly (včetně exportů, kde dávají smysl)
    private static final Set<String> AUDITOR_RO_BASE = union(
            of(DASHBOARD_VIEW),
            // Projekty, deník, rozpočet, soubory (read/download), tým (read)
            of(PROJECTS_READ, LOGBOOK_READ, BUDGET_READ, FILES_READ, FILES_DOWNLOAD, TEAM_READ),
            // Finance read-only + exporty/reporty
            of(INVOICES_READ, INVOICES_EXPORT, CUSTOMERS_READ, CUSTOMERS_EXPORT,
                    PAYMENTS_READ, PAYMENTS_EXPORT, SERIES_READ,
                    REPORTS_INVOICES_READ, REPORTS_INVOICES_EXPORT,
                    INV_VAT_REPORT, INV_VAT_EXPORT, VAT_LEDGER_READ, VAT_LEDGER_EXPORT,
                    PROFORMAS_READ, CREDIT_NOTES_READ, RECURRING_INVOICES_READ,
                    PURCHASE_INVOICES_READ)
    );

    // Integration – technické role pro synchronizace (banky, e-invoicing, webhooks)
    private static final Set<String> INTEGRATION_BASE = union(
            of(DASHBOARD_VIEW),
            of(INTEGRATIONS_MANAGE),
            INV_INTEGRATION_ADMIN, WEBHOOKS_RW, E_INVOICING_RW, BANKING_RW,
            // read pro entity, které integrace typicky potřebuje načítat
            of(INVOICES_READ, CUSTOMERS_READ, PAYMENTS_READ, SERIES_READ, REPORTS_INVOICES_READ)
    );

    // Member – základní interní uživatel (bez financí), můžeš upravit podle potřeby
    private static final Set<String> MEMBER_BASE = union(
            of(DASHBOARD_VIEW),
            of(PROJECTS_READ, LOGBOOK_READ),
            of(FILES_READ, FILES_DOWNLOAD),
            of(TEAM_READ)
    );

    // Viewer – čistě read-only minimum (včetně čtení faktur, bez exportů)
    private static final Set<String> VIEWER_BASE = of(
            DASHBOARD_VIEW,
            PROJECTS_READ, LOGBOOK_READ, FILES_READ, FILES_DOWNLOAD,
            INVOICES_READ, CUSTOMERS_READ, TEAM_READ, BUDGET_READ
    );

    /* --------------------------------------
     * Company role → scopes (MVP katalog)
     * -------------------------------------- */
    public static final Map<CompanyRoleName, Set<String>> COMPANY_ROLE_SCOPES = Map.ofEntries(
            entry(CompanyRoleName.SUPERADMIN,       SUPERADMIN_ALL),
            entry(CompanyRoleName.OWNER,            OWNER_ALL),
            entry(CompanyRoleName.COMPANY_ADMIN,    COMPANY_ADMIN_BASE),
            entry(CompanyRoleName.ACCOUNTANT,       ACCOUNTANT_BASE),
            entry(CompanyRoleName.PURCHASING,       PURCHASING_BASE),
            entry(CompanyRoleName.MANAGER,          MANAGER_BASE),
            entry(CompanyRoleName.DOC_CONTROLLER,   DOC_CONTROLLER_BASE),
            entry(CompanyRoleName.FLEET_MANAGER,    FLEET_MANAGER_BASE),
            entry(CompanyRoleName.HR_MANAGER,       HR_MANAGER_BASE),
            entry(CompanyRoleName.AUDITOR_READONLY, AUDITOR_RO_BASE),
            entry(CompanyRoleName.INTEGRATION,      INTEGRATION_BASE),
            entry(CompanyRoleName.MEMBER,           MEMBER_BASE),
            entry(CompanyRoleName.VIEWER,           VIEWER_BASE)
    );

    /* --------------------------------------
     * Project role → scopes
     * (ponecháno na další krok dle enumu ProjectRoleName)
     * -------------------------------------- */
    public static final Map<ProjectRoleName, Set<String>> PROJECT_ROLE_SCOPES = Map.of(); // viz RBAC 2.1 §1.1–1.4
}
