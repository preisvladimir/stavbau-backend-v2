// src/main/java/cz/stavbau/backend/invoices/repo/CustomerRepository.java
package cz.stavbau.backend.customers.repo;

import cz.stavbau.backend.common.simple.IdNameView;
import cz.stavbau.backend.customers.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByIdAndCompanyId(UUID id, UUID companyId);
    boolean existsByCompanyIdAndIco(UUID companyId, String ico);

    // ideálně s tenant guardem
    @Query("select c.id as id, c.name as name " +
            "from Customer c " +
            "where c.companyId = :companyId and c.id in :ids")
    List<IdNameView> findNamesByCompanyAndIdIn(@Param("companyId") UUID companyId,
                                               @Param("ids") Collection<UUID> ids);
}
