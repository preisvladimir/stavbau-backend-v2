package cz.stavbau.backend.invoices.repo;
import cz.stavbau.backend.invoices.model.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {
    List<InvoiceLine> findByInvoiceId(UUID invoiceId);
}
