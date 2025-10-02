package cz.stavbau.backend.tenants.service.impl;

import cz.stavbau.backend.tenants.repo.CompanyRepository;
import cz.stavbau.backend.tenants.service.CompanyLocaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyLocaleServiceImpl implements CompanyLocaleService {

    private final CompanyRepository companyRepository;

    @Override
    public Optional<Locale> defaultLocale(UUID companyId) {
        return companyRepository.findDefaultLocaleById(companyId)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Locale::forLanguageTag)
                .filter(loc -> !loc.getLanguage().isEmpty());
    }
}
