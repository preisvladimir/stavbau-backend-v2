package cz.stavbau.backend.tenants.repo;
import cz.stavbau.backend.tenants.model.Company; import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
public interface CompanyRepository extends JpaRepository<Company, UUID>{

    Optional<Company> findByIco(String ico);

    boolean existsByIco(String ico);
}
