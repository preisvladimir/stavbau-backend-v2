package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.invoices.dto.CustomerDto;
import cz.stavbau.backend.invoices.dto.CustomerSummaryDto;
import cz.stavbau.backend.invoices.dto.CreateCustomerRequest;
import cz.stavbau.backend.invoices.dto.UpdateCustomerRequest;
import cz.stavbau.backend.invoices.filter.CustomerFilter;
import cz.stavbau.backend.invoices.service.CustomerService;
import cz.stavbau.backend.common.i18n.I18nLocaleService;
import cz.stavbau.backend.common.persistence.PageableUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
public class CustomersController {

    private final CustomerService service;
    private final I18nLocaleService i18nLocale;

    private HttpHeaders i18nHeaders(Locale locale) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
        h.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
        return h;
    }

    @GetMapping
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(summary = "List customers (paged)")
    public ResponseEntity<PageResponse<CustomerSummaryDto>> list(
            @RequestParam(value = "q",      required = false) String q,
            @RequestParam(value = "name",   required = false) String name,
            @RequestParam(value = "ico",    required = false) String ico,
            @RequestParam(value = "dic",    required = false) String dic,
            @RequestParam(value = "email",  required = false) String email,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Pageable pageable,
            Locale locale
    ) {
        // ------------------------------------------------------
        // 1️⃣ Bezpečné řazení + fallback (vše přes utilitu)
        // ------------------------------------------------------
        var allowed = Set.of("id", "name", "ico", "dic", "email", "createdAt", "updatedAt");
        Sort sort = PageableUtils.safeSortOrDefault(pageable, Sort.by("name").ascending(), allowed);
        var paging = PageRequest.of(Math.max(page, 0), Math.min(size, 100), sort);

        // ------------------------------------------------------
        // 2️⃣ Sestavení filtru (DTO pattern)
        // ------------------------------------------------------
        var filter = new CustomerFilter();
        filter.setQ(q);
        filter.setName(name);
        filter.setIco(ico);
        filter.setDic(dic);
        filter.setEmail(email);
        filter.setActive(active);

        // ------------------------------------------------------
        // 3️⃣ Zavolání service vrstvy (read-only)
        // ------------------------------------------------------
        var pageData = service.list(filter, paging);
        var body = PageResponse.of(pageData);

        // ------------------------------------------------------
        // 4️⃣ i18n hlavičky (Vary + Content-Language)
        // ------------------------------------------------------
        return ResponseEntity.ok()
                .header(HttpHeaders.VARY, "Accept-Language")
                .header(HttpHeaders.CONTENT_LANGUAGE, locale != null ? locale.toLanguageTag() : "cs")
                .body(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(summary = "Detail zákazníka", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CustomerDto> get(@PathVariable UUID id) {
        var loc = i18nLocale.resolve();
        var dto = service.get(id);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_CREATE,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(summary = "Vytvořit zákazníka", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CreateCustomerRequest req) {
        var loc = i18nLocale.resolve();
        var dto = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + dto.getId()))
                .headers(i18nHeaders(loc))
                .body(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_UPDATE,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(summary = "Upravit zákazníka", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CustomerDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest req) {
        var loc = i18nLocale.resolve();
        var dto = service.update(id, req);
        return new ResponseEntity<>(dto, i18nHeaders(loc), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_DELETE,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(summary = "Smazat zákazníka", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        var loc = i18nLocale.resolve();
        service.delete(id);
        return ResponseEntity.noContent().headers(i18nHeaders(loc)).build();
    }
}
