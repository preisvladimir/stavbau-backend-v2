// src/main/java/cz/stavbau/backend/invoices/service/impl/CustomerServiceImpl.java
package cz.stavbau.backend.invoices.service.impl;

import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.invoices.dto.*;
import cz.stavbau.backend.invoices.mapper.CustomerMapper;
import cz.stavbau.backend.invoices.model.Customer;
import cz.stavbau.backend.invoices.repo.CustomerRepository;
import cz.stavbau.backend.invoices.service.CustomerService;
import cz.stavbau.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;
    private final CustomerMapper mapper;

    @Override
    public CustomerDto create(CreateCustomerRequest req) {
        UUID companyId = requireCompanyId();
        // jednoduchá normalizace
        var ico = req.ico() != null ? req.ico().trim() : null;
        if (ico != null && repo.existsByCompanyIdAndIco(companyId, ico)) {
            throw new ConflictException("customer.ico.exists"); // i18n key
        }
        var c = new Customer();
        c.setCompanyId(companyId);
        c.setType(req.type());
        c.setName(req.name().trim());
        c.setIco(ico);
        c.setDic(req.dic());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        // CREATE: typed billingAddress (pokud přišla)
        if (req.billingAddress() != null) {
            var addr = org.mapstruct.factory.Mappers
                    .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                    .toEntity(req.billingAddress());
            c.setBillingAddress(addr);
        }
        c.setDefaultPaymentTermsDays(req.defaultPaymentTermsDays());
        c.setNotes(req.notes());
        c = repo.save(c);
        return mapper.toDto(c);
    }

    @Override
    public CustomerDto update(UUID id, UpdateCustomerRequest req) {
        UUID companyId = requireCompanyId();
        var c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        if (req.name() != null) c.setName(req.name().trim());
        if (req.type() != null) c.setType(req.type());
        if (req.ico() != null) c.setIco(req.ico().trim());
        if (req.dic() != null) c.setDic(req.dic());
        if (req.email() != null) c.setEmail(req.email());
        if (req.phone() != null) c.setPhone(req.phone());
        // UPDATE: PATCH sémantika – pokud billingAddress != null, přepiš; jinak ponech beze změny
        if (req.billingAddress() != null) {
            var addr = org.mapstruct.factory.Mappers
                    .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                    .toEntity(req.billingAddress());
            c.setBillingAddress(addr);
        }
        if (req.defaultPaymentTermsDays() != null) c.setDefaultPaymentTermsDays(req.defaultPaymentTermsDays());
        if (req.notes() != null) c.setNotes(req.notes());
        return mapper.toDto(c);
    }

    @Override
    public void delete(UUID id) {
        UUID companyId = requireCompanyId();
        var c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        repo.delete(c); // MVP: hard delete (případně soft v budoucnu)
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto get(UUID id) {
        UUID companyId = requireCompanyId();
        var c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        return mapper.toDto(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerSummaryDto> search(String q, Pageable pageable) {
        var companyId = SecurityUtils.currentCompanyId();
        // MVP: jednoduché jméno/ICO contains – specifikace dodáme později
        return repo.findAll((root, cq, cb) -> {
            var p = cb.equal(root.get("companyId"), companyId);
            if (q != null && !q.isBlank()) {
                var like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                p = cb.and(p, cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("ico")), like),
                        cb.like(cb.lower(root.get("dic")), like)
                ));
            }
            return p;
        }, pageable).map(mapper::toSummaryDto);
    }

    private UUID requireCompanyId() {
        return SecurityUtils.currentCompanyId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("auth.company.required"));
    }
}
