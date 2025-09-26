// src/main/java/cz/stavbau/backend/invoices/repo/CustomerRepository.java
package cz.stavbau.backend.invoices.repo;

import cz.stavbau.backend.invoices.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByIdAndCompanyId(UUID id, UUID companyId);
    boolean existsByCompanyIdAndIco(UUID companyId, String ico);
}
