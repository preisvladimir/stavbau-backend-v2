// src/main/java/cz/stavbau/backend/customers/service/CustomerService.java
package cz.stavbau.backend.features.customers.service;

import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.features.customers.dto.CreateCustomerRequest;
import cz.stavbau.backend.features.customers.dto.CustomerDto;
import cz.stavbau.backend.features.customers.dto.CustomerSummaryDto;
import cz.stavbau.backend.features.customers.dto.UpdateCustomerRequest;
import cz.stavbau.backend.features.customers.filter.CustomerFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerDto create(CreateCustomerRequest req);
    CustomerDto update(UUID id, UpdateCustomerRequest req);
    void delete(UUID id);
    CustomerDto get(UUID id);

    Page<CustomerSummaryDto> list(CustomerFilter filter, Pageable pageable);

    default Page<CustomerSummaryDto> list(String q, Pageable pageable) {
        CustomerFilter f = new CustomerFilter();
        f.setQ(q);
        return list(f, pageable);
    }

    /** Lehký lookup pro async selecty (paged). */
    Page<SelectOptionDto> lookup(CustomerFilter filter, Pageable pageable);
}
