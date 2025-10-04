// src/main/java/cz/stavbau/backend/invoices/service/CustomerService.java
package cz.stavbau.backend.invoices.service;

import cz.stavbau.backend.invoices.dto.*;
import cz.stavbau.backend.invoices.filter.CustomerFilter;
import cz.stavbau.backend.projects.dto.ProjectSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerDto create(CreateCustomerRequest req);
    CustomerDto update(UUID id, UpdateCustomerRequest req);
    void delete(UUID id);
    CustomerDto get(UUID id);
    Page<CustomerSummaryDto> list(String q, Pageable pageable);

    /** Nové: typovaný filtr (doporučené používat interně) */
    Page<CustomerSummaryDto> list(CustomerFilter filter, Pageable pageable);

}
