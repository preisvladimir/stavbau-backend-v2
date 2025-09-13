package cz.stavbau.backend.invoices.service.impl;

import cz.stavbau.backend.invoices.model.Invoice;
import cz.stavbau.backend.invoices.model.InvoiceLine;
import cz.stavbau.backend.invoices.model.InvoiceStatus;
import cz.stavbau.backend.invoices.model.VatMode;
import cz.stavbau.backend.invoices.repo.InvoiceLineRepository;
import cz.stavbau.backend.invoices.repo.InvoiceRepository;
import cz.stavbau.backend.invoices.repo.InvoiceSpecs;
import cz.stavbau.backend.invoices.repo.NumberSeriesRepository;
import cz.stavbau.backend.invoices.service.InvoiceService;
import cz.stavbau.backend.invoices.service.NumberSeriesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final InvoiceLineRepository lineRepo;
    private final NumberSeriesService numberSeriesService;
    private final NumberSeriesRepository numberSeriesRepo;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepo,
                              InvoiceLineRepository lineRepo,
                              NumberSeriesService numberSeriesService,
                              NumberSeriesRepository numberSeriesRepo) {
        this.invoiceRepo = invoiceRepo;
        this.lineRepo = lineRepo;
        this.numberSeriesService = numberSeriesService;
        this.numberSeriesRepo = numberSeriesRepo;
    }

    @Override
    @Transactional
    public UUID createDraft(UUID companyId, UUID projectId, LocalDate issueDate, LocalDate dueDate, LocalDate taxDate,
                            String currency, String supplierJson, String customerJson) {
        Invoice inv = new Invoice();
        inv.setId(UUID.randomUUID());
        inv.setCompanyId(companyId);
        inv.setProjectId(projectId);
        inv.setIssueDate(issueDate);
        inv.setDueDate(dueDate);
        inv.setTaxDate(taxDate);
        inv.setCurrency(currency);
        inv.setVatMode(VatMode.STANDARD);
        inv.setSupplierJson(supplierJson);
        inv.setCustomerJson(customerJson);
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setVatTotal(BigDecimal.ZERO);
        inv.setTotal(BigDecimal.ZERO);
        inv.setStatus(InvoiceStatus.DRAFT);
        invoiceRepo.save(inv);
        return inv.getId();
    }

    @Override
    @Transactional
    public void addOrReplaceLines(UUID invoiceId, List<LineCreate> lines) {
        List<InvoiceLine> existing = lineRepo.findByInvoiceId(invoiceId);
        lineRepo.deleteAll(existing);

        for (LineCreate l : lines) {
            InvoiceLine il = new InvoiceLine();
            il.setId(UUID.randomUUID());
            il.setInvoiceId(invoiceId);
            il.setItemName(l.itemName());
            il.setQuantity(l.quantity());
            il.setUnit(l.unit());
            il.setUnitPrice(l.unitPrice());
            il.setVatRate(l.vatRate());
            BigDecimal lineTotal = l.quantity().multiply(l.unitPrice()).setScale(2, RoundingMode.HALF_UP);
            il.setLineTotal(lineTotal);
            lineRepo.save(il);
        }
        recalcTotals(invoiceId);
    }

    @Override
    @Transactional
    public void recalcTotals(UUID invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        List<InvoiceLine> lines = lineRepo.findByInvoiceId(invoiceId);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal vat = BigDecimal.ZERO;

        for (InvoiceLine il : lines) {
            subtotal = subtotal.add(il.getLineTotal());
            if (inv.getVatMode() == VatMode.STANDARD) {
                BigDecimal lineVat = il.getLineTotal().multiply(il.getVatRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                vat = vat.add(lineVat);
            }
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        vat = vat.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        inv.setSubtotal(subtotal);
        inv.setVatTotal(vat);
        inv.setTotal(total);
        invoiceRepo.save(inv);
    }

    @Override
    @Transactional
    public String issue(UUID invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (inv.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Invoice must be DRAFT to ISSUE");
        }
        String number = numberSeriesService.generateNextNumber(inv.getCompanyId(), "INV", inv.getIssueDate());
        inv.setNumber(number);
        inv.setStatus(InvoiceStatus.ISSUED);
        invoiceRepo.save(inv);
        return number;
    }

    @Override
    @Transactional
    public void markPaid(UUID invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (inv.getStatus() != InvoiceStatus.ISSUED) {
            throw new IllegalStateException("Only ISSUED can be marked PAID");
        }
        inv.setStatus(InvoiceStatus.PAID);
        invoiceRepo.save(inv);
    }

    @Override
    @Transactional
    public void cancel(UUID invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (inv.getStatus() != InvoiceStatus.ISSUED) {
            throw new IllegalStateException("Only ISSUED can be CANCELLED");
        }
        inv.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepo.save(inv);
    }

    @Override
    public InvoiceStatus getStatus(UUID invoiceId) {
        return invoiceRepo.findById(invoiceId).map(Invoice::getStatus).orElseThrow();
    }

    @Override
    public Invoice get(UUID invoiceId) {
        return invoiceRepo.findById(invoiceId).orElseThrow();
    }

    @Override
    public Page<Invoice> search(UUID companyId, Optional<UUID> projectId, Optional<InvoiceStatus> status,
                                Optional<String> q, Optional<LocalDate> dateFrom, Optional<LocalDate> dateTo,
                                Pageable pageable) {
        Specification<Invoice> spec = Specification.where(InvoiceSpecs.company(companyId));
        if (projectId.isPresent()) spec = spec.and(InvoiceSpecs.project(projectId.get()));
        if (status.isPresent()) spec = spec.and(InvoiceSpecs.status(status.get()));
        if (dateFrom.isPresent()) spec = spec.and(InvoiceSpecs.dateFrom(dateFrom.get()));
        if (dateTo.isPresent()) spec = spec.and(InvoiceSpecs.dateTo(dateTo.get()));
        if (q.isPresent() && !q.get().isBlank()) spec = spec.and(InvoiceSpecs.q(q.get()));
        return invoiceRepo.findAll(spec, pageable);
    }
}
