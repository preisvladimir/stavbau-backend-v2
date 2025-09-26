// src/main/java/cz/stavbau/backend/invoices/service/CustomerService.java
package cz.stavbau.backend.invoices.service;

import cz.stavbau.backend.invoices.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerDto create(CreateCustomerRequest req);
    CustomerDto update(UUID id, UpdateCustomerRequest req);
    void delete(UUID id);
    CustomerDto get(UUID id);
    Page<CustomerSummaryDto> search(String q, Pageable pageable);
}
