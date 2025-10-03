// src/main/java/cz/stavbau/backend/common/i18n/I18nLocaleService.java
package cz.stavbau.backend.common.i18n;

import cz.stavbau.backend.security.SecurityUtils;
import cz.stavbau.backend.tenants.service.CompanyLocaleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component("i18nLocaleService") // ⬅️ už to nebude kolidovat se jménem 'localeResolver'
@RequiredArgsConstructor
public class I18nLocaleService {

    private final Optional<CompanyLocaleService> companyLocaleService; // může být Optional

    /** ?lang → Accept-Language → user → company → appDefault(cs-CZ) */
    public Locale resolve(HttpServletRequest req) {
        String q = Optional.ofNullable(req.getParameter("lang")).map(String::trim).orElse(null);
        if (q != null && !q.isBlank()) return Locale.forLanguageTag(q);

        Locale header = req.getLocale();
        if (header != null) return header;

        var userLoc = SecurityUtils.currentUserLocale();
        if (userLoc.isPresent()) return userLoc.get();

        var companyId = SecurityUtils.currentCompanyId();
        if (companyId.isPresent() && companyLocaleService.isPresent()) {
            var fromCompany = companyLocaleService.get().defaultLocale(companyId.get());
            if (fromCompany.isPresent()) return fromCompany.get();
        }

        return Locale.forLanguageTag("cs-CZ");
    }

    /** Resolve mimo web vrstvu – z request-scoped kontextu nebo app default. */
    public Locale resolve() {
        return Optional.ofNullable(LocaleContext.get()).orElse(Locale.forLanguageTag("cs-CZ"));
    }
}
