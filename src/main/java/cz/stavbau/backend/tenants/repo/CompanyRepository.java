package cz.stavbau.backend.tenants.repo;
import cz.stavbau.backend.tenants.model.Company; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CompanyRepository extends JpaRepository<Company, UUID>{}
