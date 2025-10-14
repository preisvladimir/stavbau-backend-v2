package cz.stavbau.backend.features.customers.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.common.api.dto.SelectOptionDto;
import cz.stavbau.backend.features.customers.dto.CreateCustomerRequest;
import cz.stavbau.backend.features.customers.dto.CustomerDto;
import cz.stavbau.backend.features.customers.dto.CustomerSummaryDto;
import cz.stavbau.backend.features.customers.dto.UpdateCustomerRequest;
import cz.stavbau.backend.features.customers.filter.CustomerFilter;
import cz.stavbau.backend.features.customers.service.CustomerService;
import cz.stavbau.backend.security.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/{companyId}/customers")
@Tag(name = "Customers")
public class CustomersController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasScope('customers:read')"
    )
    @Operation(summary = "Seznam zákazníků (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<CustomerSummaryDto>> list(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute CustomerFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal,
            HttpServletRequest req
    ) {
        if (log.isDebugEnabled()) {
            Sort s = (pageable != null ? pageable.getSort() : Sort.unsorted());
            log.debug("[Customers] QS={} filter={} rawSort={}", req.getQueryString(), filter, s);
        }
        Page<CustomerSummaryDto> page = customerService.list(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasScope('customers:read')"
    )
    @Operation(summary = "Detail zákazníka")
    @Transactional(readOnly = true)
    public ResponseEntity<CustomerDto> get(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(customerService.get(id));
    }

    @PostMapping
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasScope('customers:write')"
    )
    @Operation(summary = "Vytvořit zákazníka")
    @Transactional
    public ResponseEntity<CustomerDto> create(
            @PathVariable UUID companyId,
            @RequestBody @Valid CreateCustomerRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        CustomerDto dto = customerService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasScope('customers:update')"
    )
    @Operation(summary = "Upravit zákazníka (PATCH)")
    @Transactional
    public ResponseEntity<CustomerDto> update(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCustomerRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(customerService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasScope('customers:remove')"
    )
    @Operation(summary = "Smazat zákazníka")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lookup")
    @PreAuthorize(
            "@companyGuard.sameCompany(#companyId, principal) && " +
                    "@rbac.hasScope('customers:read')"
    )
    @Operation(summary = "Lookup zákazníků pro async select (paged)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<SelectOptionDto>> lookup(
            @PathVariable UUID companyId,
            @ParameterObject @ModelAttribute CustomerFilter filter,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        Page<SelectOptionDto> page = customerService.lookup(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

}
