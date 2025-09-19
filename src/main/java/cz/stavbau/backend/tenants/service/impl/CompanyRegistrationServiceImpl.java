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

import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.i18n.Messages;

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

    private final Messages messages;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CompanyRegistrationResponse register(CompanyRegistrationRequest req) {

        // 1) Duplicitní IČO?
        if (companyRepository.existsByIco(req.company().ico())) {
            throw new ConflictException(messages.msg("company.exists"));
        }

        // 2) Duplicitní e-mail (case-insensitive)?
        var email = req.owner().email().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(messages.msg("user.email.exists"));
        }

        // 3) Company
        var c = new Company();
        c.setIco(req.company().ico());
        c.setObchodniJmeno(req.company().name());
        c.setPravniFormaCode(req.company().legalFormCode());

        var sidlo = (c.getSidlo() != null) ? c.getSidlo() : new RegisteredAddress();
        sidlo.setNazevUlice(req.company().address().street());
        sidlo.setNazevObce(req.company().address().city());
        sidlo.setPsc(req.company().address().zip());
        sidlo.setNazevStatu(req.company().address().country());
        c.setSidlo(sidlo);

        try { c = companyRepository.saveAndFlush(c); }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException(messages.msg("company.exists"), ex);
        }

        // 4) User
        var u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.owner().password()));
        u.setCompanyId(c.getId());
        try { u = userRepository.saveAndFlush(u); }
        catch (DataIntegrityViolationException ex) {
            throw new ConflictException(messages.msg("user.email.exists"), ex);
        }

        // 5) Membership (OWNER)
        var m = new CompanyMember();
        m.setCompanyId(c.getId());
        m.setUserId(u.getId());
        m.setRole(CompanyRoleName.OWNER);
        memberRepository.save(m);

        return CompanyRegistrationResponse.created(c.getId(), u.getId());
    }
}