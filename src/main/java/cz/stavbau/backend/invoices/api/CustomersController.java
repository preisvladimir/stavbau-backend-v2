package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.invoices.dto.*;
import cz.stavbau.backend.invoices.service.CustomerService;
import cz.stavbau.backend.security.rbac.Scopes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Invoices / Customers")
public class CustomersController {

    private final CustomerService service;

    @GetMapping
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ)")
    public PageResponse<CustomerSummaryDto> list(
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable
    ) {
        var page = pageable == null ? PageRequest.of(0, 20) : pageable;
        var data = service.search(q, page);
        return PageResponse.of(data.getContent(), data.getNumber(), data.getSize(), data.getTotalElements());
    }

    @GetMapping("{id}")
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ)")
    public CustomerDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_CREATE)")
    public CustomerDto create(@Valid @RequestBody CreateCustomerRequest req) {
        return service.create(req);
    }

    @PatchMapping("{id}")
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_UPDATE)")
    public CustomerDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_DELETE)")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}