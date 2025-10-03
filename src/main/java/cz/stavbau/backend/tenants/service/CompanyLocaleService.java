package cz.stavbau.backend.tenants.service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Poskytne výchozí locale firmy (pokud existuje). */
public interface CompanyLocaleService {
    Optional<Locale> defaultLocale(UUID companyId);
}
