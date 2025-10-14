package cz.stavbau.backend.features.invoices.repo;
import cz.stavbau.backend.features.invoices.model.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {
    List<InvoiceLine> findByInvoiceId(UUID invoiceId);
}
