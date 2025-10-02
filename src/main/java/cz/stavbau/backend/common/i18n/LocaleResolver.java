package cz.stavbau.backend.common.i18n;

import cz.stavbau.backend.security.SecurityUtils;
import cz.stavbau.backend.tenants.service.CompanyLocaleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocaleResolver {

    // Volitelný fallback na firemní locale (bean může i nemusí existovat)
    private final CompanyLocaleService companyLocaleService;

    /** Primární resolve pořadí: ?lang → Accept-Language → user → company → appDefault(cs-CZ) */
    public Locale resolve(HttpServletRequest req) {
        // 1) ?lang=cs|en|cs-CZ
        String q = Optional.ofNullable(req.getParameter("lang"))
                .map(String::trim).orElse(null);
        if (q != null && !q.isBlank()) return Locale.forLanguageTag(q);

        // 2) Accept-Language (nejlepší match)
        Locale header = req.getLocale();
        if (header != null) return header;

        // 3) user profile (z SecurityUtils)
        Optional<Locale> userLoc = SecurityUtils.currentUserLocale();
        if (userLoc.isPresent()) return userLoc.get();

        // 4) company default (pokud máme provider i companyId)
        var companyId = SecurityUtils.currentCompanyId();
            if (companyId.isPresent()) {
                var fromCompany = companyLocaleService.defaultLocale(companyId.get());
                if (fromCompany.isPresent()) return fromCompany.get();
            }

        // 5) app default
        return Locale.forLanguageTag("cs-CZ");
    }

    /** Resolve mimo web vrstvu – z request-scoped kontextu nebo app default. */
    public Locale resolve() {
        return Optional.ofNullable(LocaleContext.get()).orElse(Locale.forLanguageTag("cs-CZ"));
    }
}
