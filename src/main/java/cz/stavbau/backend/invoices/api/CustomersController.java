package cz.stavbau.backend.invoices.api;

import cz.stavbau.backend.common.api.PageResponse;
import cz.stavbau.backend.invoices.dto.*;
import cz.stavbau.backend.invoices.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
public class CustomersController {

    private final CustomerService service;

    @GetMapping
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(
            summary = "List zákazníků",
            description = "Vrací stránkovaný seznam zákazníků (company-scoped). Fulltext přes name/ICO/DIČ pomocí parametru `q`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizováno"),
            @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)")
    })
    public PageResponse<CustomerSummaryDto> list(
            @Parameter(description = "Fulltext (name, ICO, DIČ)") @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable
    ) {
        var page = pageable == null ? PageRequest.of(0, 20) : pageable;
        var data = service.search(q, page);
        return PageResponse.of(data);
    }

    @GetMapping("{id}")
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(
            summary = "Detail zákazníka",
            description = "Načte zákazníka podle ID v rámci aktuální company.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizováno"),
            @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)"),
            @ApiResponse(responseCode = "404", description = "Zákazník nenalezen")
    })
    public CustomerDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_CREATE,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Vytvořit zákazníka",
            description = "Založí nového zákazníka v rámci aktuální company.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vytvořeno",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Nevalidní požadavek"),
            @ApiResponse(responseCode = "401", description = "Neautorizováno"),
            @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)"),
            @ApiResponse(responseCode = "409", description = "Konflikt (např. duplicitní IČO ve firmě)")
    })
    public CustomerDto create(@Valid @RequestBody CreateCustomerRequest req) {
        return service.create(req);
    }

    @PatchMapping("{id}")
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_UPDATE,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @Operation(
            summary = "Upravit zákazníka",
            description = "Aktualizuje vybraná pole zákazníka (partial update).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Nevalidní požadavek"),
            @ApiResponse(responseCode = "401", description = "Neautorizováno"),
            @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)"),
            @ApiResponse(responseCode = "404", description = "Zákazník nenalezen"),
            @ApiResponse(responseCode = "409", description = "Konflikt (např. duplicitní IČO ve firmě)")
    })
    public CustomerDto update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("@rbac.hasAnyScope("
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_DELETE,"
            + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
            + ")")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Smazat zákazníka",
            description = "Odstraní zákazníka (MVP hard delete). Pokud existují navázané faktury, FK se nastaví na NULL (snapshot zůstává).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Smazáno"),
            @ApiResponse(responseCode = "401", description = "Neautorizováno"),
            @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)"),
            @ApiResponse(responseCode = "404", description = "Zákazník nenalezen")
    })
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    /* =========================
     * Budoucí endpointy (připraveno)
     * =========================
     * Pozn.: tyto bloky jsou zakomentované, aby nenarušily kompilaci.
     * Až budou implementované v service, odkomentuj je.
     */

    // --- Link user (klientský portál) ---
    /*
    @PostMapping("{id}/link-user/{userId}")
    @PreAuthorize("@rbac.hasAnyScope("
        + "T(cz.stavbau.backend.security.rbac.Scopes).CUSTOMERS_LINK_USER,"
        + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_WRITE"
        + ")")
    @Operation(
        summary = "Propojit zákazníka s uživatelem (client portal)",
        description = "Přiřadí existujícího uživatele k zákazníkovi (linkedUserId).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propojeno",
            content = @Content(schema = @Schema(implementation = CustomerDto.class))),
        @ApiResponse(responseCode = "400", description = "Nevalidní požadavek / už propojeno"),
        @ApiResponse(responseCode = "401", description = "Neautorizováno"),
        @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)"),
        @ApiResponse(responseCode = "404", description = "Zákazník nebo uživatel nenalezen"),
        @ApiResponse(responseCode = "409", description = "Konflikt (např. uživatel patří jiné firmě)")
    })
    public CustomerDto linkUser(@PathVariable UUID id, @PathVariable UUID userId) {
        return service.linkUser(id, userId);
    }
    */

    // --- Import (CSV/XLSX/JSON) ---
    /*
    @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).CUSTOMERS_IMPORT))")
    @Operation(
        summary = "Import zákazníků",
        description = "Načte zákazníky z nahraného souboru (CSV/XLSX/JSON) a založí/aktualizuje záznamy.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Přijato ke zpracování"),
        @ApiResponse(responseCode = "400", description = "Nevalidní formát souboru"),
        @ApiResponse(responseCode = "401", description = "Neautorizováno"),
        @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void importCustomers(@RequestPart("file") MultipartFile file) { /* service.import(file); * / }
    */

    // --- Export (CSV/XLSX/JSON) ---
    /*
    @GetMapping("export")
    @PreAuthorize("@rbac.hasAnyScope("
        + "T(cz.stavbau.backend.security.rbac.Scopes).CUSTOMERS_EXPORT,"
        + "T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_EXPORT"
        + ")")
    @Operation(
        summary = "Export zákazníků",
        description = "Exportuje seznam zákazníků ve zvoleném formátu (CSV/XLSX/JSON).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK (soubor ke stažení)"),
        @ApiResponse(responseCode = "401", description = "Neautorizováno"),
        @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)")
    })
    public ResponseEntity<Resource> exportCustomers(
            @Parameter(description = "Formát: csv|xlsx|json") @RequestParam(defaultValue = "csv") String format
    ) { /* return service.export(format); * / return null; }
    */

    // --- Suggest (typeahead) ---
    /*
    @GetMapping("suggest")
    @PreAuthorize("@rbac.hasScope(T(cz.stavbau.backend.security.rbac.Scopes).INVOICES_READ))")
    @Operation(
        summary = "Nápověda pro vyhledávání (typeahead)",
        description = "Vrací zkrácený seznam (např. top 10) zákazníků pro autocomplete podle `q`.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Neautorizováno"),
        @ApiResponse(responseCode = "403", description = "Chybí oprávnění (scope)")
    })
    public List<CustomerSuggestDto> suggest(@RequestParam String q) {
        return service.suggest(q);
    }
    */
}
