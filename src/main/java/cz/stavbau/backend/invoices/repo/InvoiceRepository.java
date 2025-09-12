package cz.stavbau.backend.invoices.repo;
import cz.stavbau.backend.invoices.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {}
