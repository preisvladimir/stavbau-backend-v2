// FILE: tenants/service/impl/CompanyRegistrationServiceImpl.java
package cz.stavbau.backend.tenants.service.impl;

import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.dto.CompanyRegistrationRequest;
import cz.stavbau.backend.tenants.dto.CompanyRegistrationResponse;
import cz.stavbau.backend.tenants.model.Company;
import cz.stavbau.backend.tenants.repo.CompanyRepository;
import cz.stavbau.backend.tenants.service.CompanyRegistrationService;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyRegistrationServiceImpl implements CompanyRegistrationService {

    private final MessageSource messages;

    // Repozitáře/servisy – přizpůsob tvému projektu:
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CompanyRegistrationResponse register(CompanyRegistrationRequest req) {
        var locale = Locale.getDefault(); // skutečně čtěte z resolveru Accept-Language

        // 1) Duplicitní IČO?
        companyRepository.findByIco(req.company().ico()).ifPresent(c -> {
            throw conflict(i18n("company.exists", locale));
        });

        // 2) Duplicitní e-mail?

        String rawEmail = req.owner().email();
        String email = rawEmail == null ? null : rawEmail.trim();

        if (email == null || email.isEmpty()) {
            // validace @NotBlank by to měla zachytit dříve; pojistka navíc
            throw conflict(i18n("errors.user.email_required", locale));
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw conflict(i18n("errors.user.email_taken", locale));
        }

        // 3) Uložit Company
        var company = new Company();
        company.setIco(req.company().ico());
        company.setDic(req.company().dic());
        company.setObchodniJmeno(req.company().name());
        company.setPravniFormaCode(req.company().legalFormCode());
        company.getSidlo().setNazevUlice(req.company().address().street());
        company.getSidlo().setNazevObce(req.company().address().city());
        company.getSidlo().setPsc(req.company().address().zip());
        company.getSidlo().setNazevStatu(req.company().address().country());

        try {
            company = companyRepository.save(company);
        } catch (DataIntegrityViolationException ex) {
            // Unikátní index nás pojistí proti závodu – vrátíme 409
            throw conflict(i18n("company.exists", locale));
        }

        // 4) Vytvořit OWNER uživatele
        var user = new User();
        user.setEmail(req.owner().email());
        user.setPasswordHash(passwordEncoder.encode(req.owner().password()));
        user.setFirstName(req.owner().firstName());
        user.setLastName(req.owner().lastName());
        user.setPhone(req.owner().phone());
        user.setCompany(company);
        user.setRole(CompanyRoleName.OWNER); // company-level role

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw conflict(i18n("user.email.exists", locale));
        }

        // 5) (Volitelně) vyemitovat domain event, e-mail atd.

        return CompanyRegistrationResponse.created(
                company.getId() != null ? company.getId() : UUID.randomUUID(), // fallback never used if entity sets ID
                user.getId()
        );
    }

    private RuntimeException conflict(String msg) {
        return new cz.stavbau.backend.shared.web.ConflictException(msg);
    }

    private String i18n(String code, Locale locale) {
        return messages.getMessage(code, null, code, locale);
    }
}
