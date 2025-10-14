package cz.stavbau.backend.features.companies.service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Poskytne výchozí locale firmy (pokud existuje). */
public interface CompanyLocaleService {
    Optional<Locale> defaultLocale(UUID companyId);
}
