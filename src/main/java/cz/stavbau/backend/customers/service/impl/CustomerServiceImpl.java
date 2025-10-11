// src/main/java/cz/stavbau/backend/customers/service/impl/CustomerServiceImpl.java
package cz.stavbau.backend.customers.service.impl;

import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.common.paging.DomainSortPolicies;
import cz.stavbau.backend.common.paging.PagingPolicy;
import cz.stavbau.backend.common.util.BusinessIdUtils;
import cz.stavbau.backend.customers.dto.*;
import cz.stavbau.backend.customers.filter.CustomerFilter;
import cz.stavbau.backend.customers.filter.CustomerFilters;
import cz.stavbau.backend.customers.mapper.CustomerMapper;
import cz.stavbau.backend.customers.model.Customer;
import cz.stavbau.backend.customers.repo.CustomerRepository;
import cz.stavbau.backend.customers.repo.spec.CustomerSpecification;
import cz.stavbau.backend.customers.service.CustomerService;
import cz.stavbau.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;
    private final CustomerMapper mapper;

    @Override
    @Transactional
    public CustomerDto create(CreateCustomerRequest req) {
        UUID companyId = SecurityUtils.requireCompanyId();

        // ICO normalize + unikátnost v rámci společnosti
        String ico = BusinessIdUtils.normalizeIco(req.ico());
        if (ico != null && repo.existsByCompanyIdAndIco(companyId, ico)) {
            throw new ConflictException("customer.ico.exists");
        }

        Customer c = new Customer();
        c.setCompanyId(companyId);
        c.setType(req.type());
        c.setName(req.name().trim());
        c.setIco(ico);
        c.setDic(req.dic());
        c.setEmail(req.email());
        c.setPhone(req.phone());

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
    @Transactional
    public CustomerDto update(UUID id, UpdateCustomerRequest req) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Customer c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));

        if (req.name() != null) c.setName(req.name().trim());
        if (req.type() != null) c.setType(req.type());
        if (req.ico() != null) {
            String newIco = BusinessIdUtils.normalizeIco(req.ico());
            if (newIco != null && !newIco.equals(c.getIco())
                    && repo.existsByCompanyIdAndIco(companyId, newIco)) {
                throw new ConflictException("customer.ico.exists");
            }
            c.setIco(newIco);
        }
        if (req.dic() != null) c.setDic(req.dic());
        if (req.email() != null) c.setEmail(req.email());
        if (req.phone() != null) c.setPhone(req.phone());

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
    @Transactional
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Customer c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        repo.delete(c); // MVP: hard delete
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto get(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        Customer c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        return mapper.toDto(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerSummaryDto> list(CustomerFilter filter, Pageable pageable) {
        // 1) Tenant guard
        UUID companyId = SecurityUtils.requireCompanyId();

        // 2) Normalizace filtrů
        CustomerFilter norm = CustomerFilters.normalize(filter);

        // 3) Paging + safe sort
        PageRequest paging = PagingPolicy.ensure(
                pageable,
                DomainSortPolicies.CUSTOMER_MAX_PAGE_SIZE,
                DomainSortPolicies.CUSTOMER_DEFAULT_SORT,
                DomainSortPolicies.CUSTOMER_ALLOWED_SORT
        );

        if (log.isDebugEnabled()) {
            log.debug("[CustomerService] companyId={} effFilter={} page={} size={} sort={}",
                    companyId, norm, paging.getPageNumber(), paging.getPageSize(), paging.getSort());
        }

        // 4) Spec + mapování
        Specification<Customer> spec = new CustomerSpecification(companyId, norm);
        Page<CustomerSummaryDto> dtoPage = repo.findAll(spec, paging).map(mapper::toSummaryDto);

        // 5) UX fallback (prázdná stránka uprostřed)
        if (dtoPage.isEmpty() && dtoPage.getTotalElements() > 0 && paging.getPageNumber() > 0) {
            PageRequest prev = PageRequest.of(paging.getPageNumber() - 1, paging.getPageSize(), paging.getSort());
            dtoPage = repo.findAll(spec, prev).map(mapper::toSummaryDto);
        }

        return dtoPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SelectOptionDto> lookup(CustomerFilter filter, Pageable pageable) {
        UUID companyId = SecurityUtils.requireCompanyId();
        CustomerFilter norm = CustomerFilters.normalize(filter);

        // menší, svižnější cap pro lookup (např. 50)
        PageRequest paging = PagingPolicy.ensure(
                pageable,
                Math.min(50, DomainSortPolicies.CUSTOMER_MAX_PAGE_SIZE),
                DomainSortPolicies.CUSTOMER_DEFAULT_SORT,  // name ASC
                DomainSortPolicies.CUSTOMER_ALLOWED_SORT   // id,name,email,phone,ico,dic,createdAt,updatedAt
        );

        Specification<Customer> spec = new CustomerSpecification(companyId, norm);
        Page<Customer> page = repo.findAll(spec, paging);

        return page.map(c -> {
            String base =
                    (c.getName() != null && !c.getName().isBlank()) ? c.getName().trim() :
                            (c.getEmail() != null && !c.getEmail().isBlank()) ? c.getEmail().trim() :
                                    c.getId().toString();
            String label = (c.getIco() != null && !c.getIco().isBlank())
                    ? base + " (" + c.getIco().trim() + ")"
                    : base;
            return new SelectOptionDto(c.getId(), label);
        });
    }
}
