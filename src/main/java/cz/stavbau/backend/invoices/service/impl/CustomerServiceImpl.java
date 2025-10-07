// src/main/java/cz/stavbau/backend/invoices/service/impl/CustomerServiceImpl.java
package cz.stavbau.backend.invoices.service.impl;

import cz.stavbau.backend.common.exception.ConflictException;
import cz.stavbau.backend.common.exception.NotFoundException;
import cz.stavbau.backend.common.util.BusinessIdUtils;
import cz.stavbau.backend.invoices.dto.*;
import cz.stavbau.backend.invoices.filter.CustomerFilter;
import cz.stavbau.backend.invoices.mapper.CustomerMapper;
import cz.stavbau.backend.invoices.model.Customer;
import cz.stavbau.backend.invoices.repo.CustomerRepository;
import cz.stavbau.backend.invoices.repo.spec.CustomerSpecification;
import cz.stavbau.backend.invoices.service.CustomerService;
import cz.stavbau.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
/**
  * Aplikační služba pro entitu {@link Customer}.
  * <p>
  * Zajišťuje:
  * <ul>
  *   <li>vytváření, čtení, aktualizaci a mazání zákazníků v rámci přihlášené společnosti (tenancy guard přes {@link SecurityUtils#requireCompanyId()});</li>
  *   <li>normalizaci identifikátorů (např. IČO) pomocí {@link BusinessIdUtils};</li>
  *   <li>konzistentní mapování na DTO přes {@link CustomerMapper};</li>
  *   <li>listování s filtrováním pomocí {@link CustomerSpecification}.</li>
  * </ul>
  * <strong>Pozn.:</strong> Business pravidla týkající se unikátnosti IČO vrací chybu 409 (Conflict) s kódem
  * <code>customer.ico.exists</code>, kterou globální handler mapuje na ProblemDetails.
  */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;
    private final CustomerMapper mapper;

    @Override
    /**
     * Vytvoří nového zákazníka v rámci aktuální společnosti.
     * <ul>
     *   <li>IČO je před uložením normalizováno (oříznutí okrajů, odstranění mezer; prázdné ⇒ {@code null}).</li>
     *   <li>Pokud je IČO vyplněné a v rámci společnosti už existuje, vyhodí se {@link ConflictException} s kódem
     *   <code>customer.ico.exists</code>.</li>
     * </ul>
     *
     * @param req vstupní DTO s daty zákazníka
     * @return vytvořený zákazník jako {@link CustomerDto}
     * @throws ConflictException pokud existuje zákazník se stejným IČO v rámci společnosti
     */
    public CustomerDto create(CreateCustomerRequest req) {
        UUID companyId = SecurityUtils.requireCompanyId();
        // normalizace: trim + odstranění mezer; prázdné => null (nevyvolá konflikt, uloží se NULL)
        var ico = BusinessIdUtils.normalizeIco(req.ico());
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
    /**
     * Aktualizuje existujícího zákazníka (PATCH sémantika).
     * <ul>
     *   <li>Pokud je součástí požadavku IČO, je normalizováno; prázdné ⇒ {@code null}.</li>
     *   <li>Duplicita IČO se kontroluje pouze v případě, že je nové IČO vyplněné a liší se od stávajícího.</li>
     * </ul>
     *
     * @param id  identifikátor zákazníka
     * @param req změny ke zpracování
     * @return aktualizovaný zákazník jako {@link CustomerDto}
     * @throws NotFoundException   pokud zákazník neexistuje v rámci společnosti
     * @throws ConflictException   pokud nové IČO koliduje s jiným zákazníkem
     */
    public CustomerDto update(UUID id, UpdateCustomerRequest req) {
        UUID companyId = SecurityUtils.requireCompanyId();
        var c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        if (req.name() != null) c.setName(req.name().trim());
        if (req.type() != null) c.setType(req.type());
        if (req.ico() != null) {
            // normalizace: trim + odstranění mezer; prázdné => null
            var newIco = BusinessIdUtils.normalizeIco(req.ico());
            // duplicitu kontrolujeme jen když se IČO opravdu mění a je vyplněné
            if (newIco != null && !newIco.equals(c.getIco())
                    && repo.existsByCompanyIdAndIco(companyId, newIco)) {
                throw new ConflictException("customer.ico.exists");
            }
            c.setIco(newIco);
        }
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
    /**
     * Smaže zákazníka v rámci aktuální společnosti.
     *
     * @param id identifikátor zákazníka
     * @throws NotFoundException pokud zákazník neexistuje v rámci společnosti
     */
    public void delete(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        var c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        repo.delete(c); // MVP: hard delete (případně soft v budoucnu)
    }

    @Override
    @Transactional
    /**
     * Vrátí detail zákazníka v rámci aktuální společnosti.
     *
     * @param id identifikátor zákazníka
     * @return {@link CustomerDto} s detailem
     * @throws NotFoundException pokud zákazník neexistuje v rámci společnosti
     */
    public CustomerDto get(UUID id) {
        UUID companyId = SecurityUtils.requireCompanyId();
        var c = repo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("customer.notFound"));
        return mapper.toDto(c);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    /**
     * Zjednodušené listování zákazníků – pouze dle fulltextu {@code q}.
     * Interně deleguje na {@link #list(CustomerFilter, Pageable)}.
     *
     * @param q        fulltext (např. část názvu, IČO, e-mail)
     * @param pageable stránkování a řazení
     * @return stránkovaný seznam {@link CustomerSummaryDto}
     */
    public Page<CustomerSummaryDto> list(String q, Pageable pageable) {
        CustomerFilter f = new CustomerFilter();
        f.setQ(q);
        return list(f, pageable);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    /**
     * Listování zákazníků s bohatším filtrováním.
     *
     * @param filter   filtrační kritéria (stav, typ, q, …)
     * @param pageable stránkování a řazení
     * @return stránkovaný seznam {@link CustomerSummaryDto}
     */
    public Page<CustomerSummaryDto> list(CustomerFilter filter, Pageable pageable) {
        UUID companyId = SecurityUtils.requireCompanyId();
        var spec = new CustomerSpecification(companyId, filter);
        return repo.findAll(spec, pageable).map(mapper::toSummaryDto);
    }

}
