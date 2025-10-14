package cz.stavbau.backend.features.companies.service.impl;

import cz.stavbau.backend.features.companies.repo.CompanyRepository;
import cz.stavbau.backend.features.companies.service.CompanyLocaleService;
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
