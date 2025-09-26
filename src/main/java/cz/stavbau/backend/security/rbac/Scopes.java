package cz.stavbau.backend.security.rbac;

/**
 * Jediné místo s textovými konstantami scopes (RBAC 2.1 §2).
 * Formát: "area:action" (lowercase).
 *
 * Pozn.: Scopes jsou navrženy tak, aby pokryly MVP i PRO rozšíření
 * (moduly Invoices/Finance, Customers, Integrace, Reporting, atd.).
 */
public final class Scopes {

    private Scopes() {}

    // Dashboard
    public static final String DASHBOARD_VIEW = "dashboard:view";

    // Projects
    public static final String PROJECTS_READ    = "projects:read";
    public static final String PROJECTS_CREATE  = "projects:create";
    public static final String PROJECTS_UPDATE  = "projects:update";
    public static final String PROJECTS_DELETE  = "projects:delete";
    public static final String PROJECTS_ARCHIVE = "projects:archive";
    public static final String PROJECTS_ASSIGN  = "projects:assign";

    // Logbook (Deník)
    public static final String LOGBOOK_READ   = "logbook:read";
    public static final String LOGBOOK_CREATE = "logbook:create";
    public static final String LOGBOOK_UPDATE = "logbook:update";
    public static final String LOGBOOK_DELETE = "logbook:delete";
    public static final String LOGBOOK_EXPORT = "logbook:export";

    // Budget
    public static final String BUDGET_READ    = "budget:read";
    public static final String BUDGET_CREATE  = "budget:create";
    public static final String BUDGET_UPDATE  = "budget:update";
    public static final String BUDGET_DELETE  = "budget:delete";
    public static final String BUDGET_APPROVE = "budget:approve";
    public static final String BUDGET_EXPORT  = "budget:export";

    // Files (obecný modul souborů)
    public static final String FILES_READ     = "files:read";
    public static final String FILES_UPLOAD   = "files:upload";
    public static final String FILES_UPDATE   = "files:update";
    public static final String FILES_DELETE   = "files:delete";
    public static final String FILES_DOWNLOAD = "files:download";
    public static final String FILES_SHARE    = "files:share";

    // Team
    public static final String TEAM_READ        = "team:read";
    public static final String TEAM_WRITE       = "team:write";
    public static final String TEAM_ADD         = "team:add";
    public static final String TEAM_REMOVE      = "team:remove";
    public static final String TEAM_UPDATE      = "team:update";
    public static final String TEAM_UPDATE_ROLE = "team:update_role";

    // Admin / Integrations (globální)
    public static final String ADMIN_USERS_READ    = "admin:users_read";
    public static final String ADMIN_USERS_MANAGE  = "admin:users_manage";
    public static final String INTEGRATIONS_MANAGE = "integrations:manage";

    // ============================
    // Invoices – Jádro (sales)
    // ============================
    // Shorthand (pokud někde používáme toggly "write"):
    public static final String INVOICES_WRITE          = "invoices:write";

    public static final String INVOICES_READ           = "invoices:read";
    public static final String INVOICES_CREATE         = "invoices:create";
    public static final String INVOICES_UPDATE         = "invoices:update";
    public static final String INVOICES_DELETE         = "invoices:delete";
    public static final String INVOICES_EXPORT         = "invoices:export";          // CSV/ISDOC/PDF balíčky
    public static final String INVOICES_APPROVE        = "invoices:approve";         // workflow před vystavením
    public static final String INVOICES_ISSUE          = "invoices:issue";           // vystavit (finalizace)
    public static final String INVOICES_CANCEL         = "invoices:cancel";          // storno
    public static final String INVOICES_DUPLICATE      = "invoices:duplicate";
    public static final String INVOICES_CONVERT_PROF   = "invoices:convert_proforma";// z proformy na fakturu
    public static final String INVOICES_CREATE_CN      = "invoices:create_credit_note";
    public static final String INVOICES_MARK_PAID      = "invoices:mark_paid";
    public static final String INVOICES_UNMARK_PAID    = "invoices:unmark_paid";
    public static final String INVOICES_CHANGE_DUE     = "invoices:change_due_date";
    public static final String INVOICES_LOCK           = "invoices:lock";            // uzamčení období/dokladu
    public static final String INVOICES_PDF            = "invoices:pdf";             // generace PDF
    public static final String INVOICES_EMAIL_PREVIEW  = "invoices:email_preview";
    public static final String INVOICES_EMAIL_SEND     = "invoices:email_send";

    // (Volitelné rozdělení zvlášť pro kredity / storna – kdo chce jemnější řízení)
    public static final String CREDIT_NOTES_READ   = "credit_notes:read";
    public static final String CREDIT_NOTES_CREATE = "credit_notes:create";
    public static final String CREDIT_NOTES_UPDATE = "credit_notes:update";
    public static final String CREDIT_NOTES_DELETE = "credit_notes:delete";
    public static final String CREDIT_NOTES_ISSUE  = "credit_notes:issue";
    public static final String CREDIT_NOTES_CANCEL = "credit_notes:cancel";
    public static final String CREDIT_NOTES_EXPORT = "credit_notes:export";

    // Proforma faktury (oddělení od běžných faktur – běžná praxe)
    public static final String PROFORMAS_READ     = "proformas:read";
    public static final String PROFORMAS_CREATE   = "proformas:create";
    public static final String PROFORMAS_UPDATE   = "proformas:update";
    public static final String PROFORMAS_DELETE   = "proformas:delete";
    public static final String PROFORMAS_ISSUE    = "proformas:issue";
    public static final String PROFORMAS_CANCEL   = "proformas:cancel";
    public static final String PROFORMAS_EXPORT   = "proformas:export";

    // Recurring (pravidelné faktury)
    public static final String RECURRING_INVOICES_READ   = "recurring_invoices:read";
    public static final String RECURRING_INVOICES_CREATE = "recurring_invoices:create";
    public static final String RECURRING_INVOICES_UPDATE = "recurring_invoices:update";
    public static final String RECURRING_INVOICES_DELETE = "recurring_invoices:delete";
    public static final String RECURRING_INVOICES_RUN    = "recurring_invoices:run";     // manuální spuštění
    public static final String RECURRING_INVOICES_PAUSE  = "recurring_invoices:pause";
    public static final String RECURRING_INVOICES_RESUME = "recurring_invoices:resume";
    public static final String RECURRING_INVOICES_PREVIEW= "recurring_invoices:preview";

    // Customers (samostatný modul/oblast – dnes může běžet pod invoices:*, ale připravujeme split)
    public static final String CUSTOMERS_READ      = "customers:read";
    public static final String CUSTOMERS_CREATE    = "customers:create";
    public static final String CUSTOMERS_UPDATE    = "customers:update";
    public static final String CUSTOMERS_DELETE    = "customers:delete";
    public static final String CUSTOMERS_IMPORT    = "customers:import";
    public static final String CUSTOMERS_EXPORT    = "customers:export";
    public static final String CUSTOMERS_LINK_USER = "customers:link_user"; // klientský portál

    // Invoice lines (položky)
    public static final String INVOICE_LINES_READ     = "invoice_lines:read";
    public static final String INVOICE_LINES_CREATE   = "invoice_lines:create";
    public static final String INVOICE_LINES_UPDATE   = "invoice_lines:update";
    public static final String INVOICE_LINES_DELETE   = "invoice_lines:delete";
    public static final String INVOICE_LINES_REORDER  = "invoice_lines:reorder";
    public static final String INVOICE_LINES_DISCOUNT = "invoice_lines:discount";
    public static final String INVOICE_LINES_TAX_OVR  = "invoice_lines:tax_override";

    // Number series (číselné řady)
    public static final String SERIES_READ      = "invoice_series:read";
    public static final String SERIES_CONFIGURE = "invoice_series:configure";
    public static final String SERIES_RESERVE   = "invoice_series:reserve";
    public static final String SERIES_RELEASE   = "invoice_series:release";
    public static final String SERIES_RESET     = "invoice_series:reset";
    public static final String SERIES_IMPORT    = "invoice_series:import";
    public static final String SERIES_EXPORT    = "invoice_series:export";

    // ============================
    // Finance – rozšíření (PRO)
    // ============================

    // Nákupní faktury / přijaté doklady (Purchase Invoices / Bills)
    public static final String PURCHASE_INVOICES_READ        = "purchase_invoices:read";
    public static final String PURCHASE_INVOICES_CREATE      = "purchase_invoices:create";
    public static final String PURCHASE_INVOICES_UPDATE      = "purchase_invoices:update";
    public static final String PURCHASE_INVOICES_DELETE      = "purchase_invoices:delete";
    public static final String PURCHASE_INVOICES_APPROVE     = "purchase_invoices:approve";
    public static final String PURCHASE_INVOICES_MARK_PAID   = "purchase_invoices:mark_paid";
    public static final String PURCHASE_INVOICES_UNMARK_PAID = "purchase_invoices:unmark_paid";
    public static final String PURCHASE_INVOICES_EXPORT      = "purchase_invoices:export";

    // Katalog položek / služby (pro prefill řádků)
    public static final String CATALOG_READ     = "catalog:read";
    public static final String CATALOG_CREATE   = "catalog:create";
    public static final String CATALOG_UPDATE   = "catalog:update";
    public static final String CATALOG_DELETE   = "catalog:delete";
    public static final String CATALOG_IMPORT   = "catalog:import";
    public static final String CATALOG_EXPORT   = "catalog:export";

    // Ceníky (price lists)
    public static final String PRICE_LISTS_READ    = "price_lists:read";
    public static final String PRICE_LISTS_CREATE  = "price_lists:create";
    public static final String PRICE_LISTS_UPDATE  = "price_lists:update";
    public static final String PRICE_LISTS_DELETE  = "price_lists:delete";
    public static final String PRICE_LISTS_ASSIGN  = "price_lists:assign"; // napojení na projekty/klienty

    // Daně / sazby / jednotky / měny / kurzy
    public static final String TAXES_READ        = "taxes:read";
    public static final String TAXES_UPDATE      = "taxes:update";
    public static final String UNITS_READ        = "units:read";
    public static final String UNITS_UPDATE      = "units:update";
    public static final String CURRENCIES_READ   = "currencies:read";
    public static final String CURRENCIES_UPDATE = "currencies:update";
    public static final String FX_RATES_READ     = "exchange_rates:read";
    public static final String FX_RATES_SYNC     = "exchange_rates:sync";

    // Platební brány / odkazy na platbu
    public static final String PAYMENT_LINKS_CREATE = "payment_links:create";
    public static final String PAYMENT_LINKS_CANCEL = "payment_links:cancel";
    public static final String PAYMENT_GATEWAYS_MNG = "payment_gateways:manage";

    // Bankovní účty / výpisy / párování
    public static final String BANK_ACCOUNTS_READ      = "bank_accounts:read";
    public static final String BANK_ACCOUNTS_MANAGE    = "bank_accounts:manage";
    public static final String BANK_STATEMENTS_IMPORT  = "bank_statements:import";
    public static final String BANK_STATEMENTS_DELETE  = "bank_statements:delete";
    public static final String BANK_RECONCILIATION_RUN = "bank_reconciliation:run";
    public static final String BANK_RECONCILIATION_UNDO= "bank_reconciliation:undo";

    // Upomínky (dunning) & připomínky
    public static final String DUNNING_READ      = "invoices_dunning:read";
    public static final String DUNNING_CONFIGURE = "invoices_dunning:configure";
    public static final String DUNNING_SEND      = "invoices_dunning:send";
    public static final String DUNNING_SCHEDULE  = "invoices_dunning:schedule";
    public static final String DUNNING_CANCEL    = "invoices_dunning:cancel";

    // Nastavení & integrace (fakturace)
    public static final String INV_SETTINGS_READ      = "invoices_settings:read";
    public static final String INV_SETTINGS_UPDATE    = "invoices_settings:update";
    public static final String INV_INTEGRATION_CONN   = "invoices_integration:connect";
    public static final String INV_INTEGRATION_SYNC   = "invoices_integration:sync";
    public static final String INV_INTEGRATION_EXPORT = "invoices_integration:export";
    public static final String INV_INTEGRATION_IMPORT = "invoices_integration:import";

    // E-fakturace (ISDOC/Peppol apod.)
    public static final String E_INVOICING_SEND     = "e_invoicing:send";
    public static final String E_INVOICING_VERIFY   = "e_invoicing:verify";
    public static final String E_INVOICING_SETTINGS = "e_invoicing:settings";

    // VAT / kontrolní hlášení / výkazy (volitelné)
    public static final String INV_VAT_REPORT  = "invoices_vat:report";
    public static final String INV_VAT_EXPORT  = "invoices_vat:export";
    public static final String VAT_LEDGER_READ = "vat_ledger:read";   // např. souhrny pro účetní
    public static final String VAT_LEDGER_EXPORT = "vat_ledger:export";

    // Reporty & analytika
    public static final String REPORTS_INVOICES_READ   = "reports_invoices:read";
    public static final String REPORTS_INVOICES_EXPORT = "reports_invoices:export";

    // I18n (faktury)
    public static final String INVOICES_I18N_READ   = "invoices_i18n:read";
    public static final String INVOICES_I18N_UPDATE = "invoices_i18n:update";

    // Šablony (PDF, e-maily)
    public static final String TEMPLATES_READ     = "templates:read";
    public static final String TEMPLATES_UPDATE   = "templates:update";
    public static final String TEMPLATES_IMPORT   = "templates:import";
    public static final String TEMPLATES_EXPORT   = "templates:export";

    // Webhooky (události fakturace)
    public static final String WEBHOOKS_READ   = "webhooks:read";
    public static final String WEBHOOKS_CREATE = "webhooks:create";
    public static final String WEBHOOKS_DELETE = "webhooks:delete";
    public static final String WEBHOOKS_TEST   = "webhooks:test";

    // Payments
    public static final String PAYMENTS_WRITE        = "payments:write"; // meta-scope (record/update/delete/reconcile/...)
    public static final String PAYMENTS_READ         = "payments:read";
    public static final String PAYMENTS_RECORD       = "payments:record";
    public static final String PAYMENTS_UPDATE       = "payments:update";
    public static final String PAYMENTS_DELETE       = "payments:delete";
    public static final String PAYMENTS_RECONCILE    = "payments:reconcile";
    public static final String PAYMENTS_WRITEOFF     = "payments:writeoff";
    public static final String PAYMENTS_REFUND       = "payments:refund";
    public static final String PAYMENTS_EXPORT       = "payments:export";
}
