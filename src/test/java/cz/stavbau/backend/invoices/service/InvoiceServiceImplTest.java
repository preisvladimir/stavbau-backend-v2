package cz.stavbau.backend.invoices.service;

import cz.stavbau.backend.features.invoices.model.Invoice;
import cz.stavbau.backend.features.invoices.model.InvoiceStatus;
import cz.stavbau.backend.features.invoices.model.VatMode;
import cz.stavbau.backend.features.invoices.repo.InvoiceLineRepository;
import cz.stavbau.backend.features.invoices.repo.InvoiceRepository;
import cz.stavbau.backend.features.invoices.repo.NumberSeriesRepository;
import cz.stavbau.backend.features.invoices.service.InvoiceService;
import cz.stavbau.backend.features.invoices.service.NumberSeriesService;
import cz.stavbau.backend.features.invoices.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceImplTest {

    private InvoiceRepository invoiceRepo;
    private InvoiceLineRepository lineRepo;
    private NumberSeriesService numberSeriesService;
    private NumberSeriesRepository numberSeriesRepo;
    private InvoiceService service;

    private final UUID COMPANY = UUID.fromString("00000000-0000-0000-0000-0000000000A1");

    @BeforeEach
    void setup() {
        invoiceRepo = Mockito.mock(InvoiceRepository.class);
        lineRepo = Mockito.mock(InvoiceLineRepository.class);
        numberSeriesService = Mockito.mock(NumberSeriesService.class);
        numberSeriesRepo = Mockito.mock(NumberSeriesRepository.class);
        service = new InvoiceServiceImpl(invoiceRepo, lineRepo, numberSeriesService, numberSeriesRepo);
    }

    @Test
    void createDraft_persists_invoice() {
        Mockito.when(invoiceRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArguments()[0]);
        UUID id = service.createDraft(COMPANY, null, LocalDate.of(2025,9,12), LocalDate.of(2025,9,30), null, "CZK", "{}", "{}");
        assertNotNull(id);
    }

    @Test
    void addOrReplaceLines_and_recalcTotals_computes_vat() {
        // Prepare invoice
        Invoice inv = new Invoice();
        inv.setId(UUID.randomUUID());
        inv.setCompanyId(COMPANY);
        inv.setCurrency("CZK");
        inv.setVatMode(VatMode.STANDARD);
        inv.setIssueDate(LocalDate.of(2025,9,12));
        inv.setDueDate(LocalDate.of(2025,9,30));
        inv.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepo.findById(eq(inv.getId()))).thenReturn(Optional.of(inv));
        when(lineRepo.findByInvoiceId(eq(inv.getId()))).thenReturn(new ArrayList<>());
        when(invoiceRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.addOrReplaceLines(inv.getId(), List.of(
            new InvoiceService.LineCreate("Práce", new BigDecimal("10"), "hod", new BigDecimal("1000"), new BigDecimal("21")),
            new InvoiceService.LineCreate("Materiál", new BigDecimal("2"), "ks", new BigDecimal("1500"), new BigDecimal("21"))
        ));

        // After replace, recalcTotals called internally -> check saved totals
        ArgumentCaptor<Invoice> cap = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepo, atLeastOnce()).save(cap.capture());
        Invoice saved = cap.getValue();

        // subtotal: 10*1000 + 2*1500 = 10000 + 3000 = 13000
        assertEquals(new BigDecimal("13000.00"), saved.getSubtotal());
        // vat: 21% z 13000 = 2730.00 (po řádcích stejné)
        assertEquals(new BigDecimal("2730.00"), saved.getVatTotal());
        // total: 15730.00
        assertEquals(new BigDecimal("15730.00"), saved.getTotal());
    }

    @Test
    void issue_assigns_number_and_sets_ISSUED() {
        Invoice inv = new Invoice();
        inv.setId(UUID.randomUUID());
        inv.setCompanyId(COMPANY);
        inv.setIssueDate(LocalDate.of(2025,9,12));
        inv.setDueDate(LocalDate.of(2025,9,30));
        inv.setVatMode(VatMode.STANDARD);
        inv.setCurrency("CZK");
        inv.setSupplierJson("{}"); inv.setCustomerJson("{}");
        inv.setSubtotal(BigDecimal.ZERO); inv.setVatTotal(BigDecimal.ZERO); inv.setTotal(BigDecimal.ZERO);
        inv.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepo.findById(eq(inv.getId()))).thenReturn(Optional.of(inv));
        when(invoiceRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(numberSeriesService.generateNextNumber(eq(COMPANY), eq("INV"), any(LocalDate.class))).thenReturn("INV-2025-0001");

        String number = service.issue(inv.getId());
        assertEquals("INV-2025-0001", number);
        assertEquals(InvoiceStatus.ISSUED, inv.getStatus());
    }

    @Test
    void markPaid_only_from_ISSUED() {
        Invoice inv = new Invoice();
        inv.setId(UUID.randomUUID());
        inv.setCompanyId(COMPANY);
        inv.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepo.findById(eq(inv.getId()))).thenReturn(Optional.of(inv));
        when(invoiceRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.markPaid(inv.getId());
        assertEquals(InvoiceStatus.PAID, inv.getStatus());
    }

    @Test
    void cancel_only_from_ISSUED() {
        Invoice inv = new Invoice();
        inv.setId(UUID.randomUUID());
        inv.setCompanyId(COMPANY);
        inv.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepo.findById(eq(inv.getId()))).thenReturn(Optional.of(inv));
        when(invoiceRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.cancel(inv.getId());
        assertEquals(InvoiceStatus.CANCELLED, inv.getStatus());
    }
}
