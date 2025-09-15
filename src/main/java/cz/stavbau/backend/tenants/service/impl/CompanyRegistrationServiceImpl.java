package cz.stavbau.backend.tenants.service.impl;

import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.security.rbac.CompanyRoleName;
import cz.stavbau.backend.tenants.dto.CompanyRegistrationRequest;
import cz.stavbau.backend.tenants.dto.CompanyRegistrationResponse;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;

import cz.stavbau.backend.tenants.membership.repo.CompanyMemberRepository;
import cz.stavbau.backend.tenants.model.Company;
import cz.stavbau.backend.tenants.model.RegisteredAddress;
import cz.stavbau.backend.tenants.repo.CompanyRepository;
import cz.stavbau.backend.tenants.service.CompanyRegistrationService;
import cz.stavbau.backend.users.model.User;
import cz.stavbau.backend.users.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CompanyRegistrationServiceImpl implements CompanyRegistrationService {

    private final MessageSource messages;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CompanyRegistrationResponse register(CompanyRegistrationRequest req) {
        var locale = Locale.getDefault(); // v reálu čti z vašeho LocaleResolveru

        // 1) Duplicitní ICO?
        if (companyRepository.existsByIco(req.company().ico())) {
            throw new ConflictException(msg("company.exists", locale));
        }

        // 2) Duplicitní e-mail (case-insensitive)?
        var email = req.owner().email().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(msg("user.email.exists", locale));
        }

        // 3) Company
        var c = new Company();
        c.setIco(req.company().ico());
       // c.setDic(req.company().dic());
        c.setObchodniJmeno(req.company().name());
        c.setPravniFormaCode(req.company().legalFormCode());
// bezpečně založ Sidlo
        var sidlo = (c.getSidlo() != null) ? c.getSidlo() : new RegisteredAddress();
        sidlo.setNazevUlice(req.company().address().street());
        sidlo.setNazevObce(req.company().address().city());
        sidlo.setPsc(req.company().address().zip());
        sidlo.setNazevStatu(req.company().address().country());
        c.setSidlo(sidlo);

        try { c = companyRepository.saveAndFlush(c); }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException(msg("company.exists", locale));
        }

        // 4) User (Auth-only data + companyId)
        var u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.owner().password()));
        u.setCompanyId(c.getId());
        try { u = userRepository.saveAndFlush(u); }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException(msg("user.email.exists", locale));
        }

        // 5) Membership (OWNER)
        var m = new CompanyMember();
        m.setCompanyId(c.getId());
        m.setUserId(u.getId());
        m.setRole(CompanyRoleName.OWNER);
        memberRepository.save(m);

        return CompanyRegistrationResponse.created(c.getId(), u.getId());
    }

    private String msg(String code, Locale locale) {
        return messages.getMessage(code, null, code, locale);
    }

    // Jednoduchá doménová výjimka pro 409 (ApiExceptionHandler ji zmapuje na RFC7807)
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }
}
