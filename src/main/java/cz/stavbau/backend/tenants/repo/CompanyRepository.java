package cz.stavbau.backend.tenants.repo;
import cz.stavbau.backend.tenants.model.Company; import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
public interface CompanyRepository extends JpaRepository<Company, UUID>{

    Optional<Company> findByIco(String ico);

    boolean existsByIco(String ico);

    @Query("select c.defaultLocale from Company c where c.id = :id")
    Optional<String> findDefaultLocaleById(@Param("id") UUID id);
}
