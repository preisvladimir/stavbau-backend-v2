package cz.stavbau.backend.tenants.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Dočasná implementace – nevrací nic (fallback v resolveru půjde na app default). */
@Component
@Primary
public class NoopCompanyLocaleService implements CompanyLocaleService {
    @Override
    public Optional<Locale> defaultLocale(UUID companyId) {
        return Optional.empty();
    }
}
