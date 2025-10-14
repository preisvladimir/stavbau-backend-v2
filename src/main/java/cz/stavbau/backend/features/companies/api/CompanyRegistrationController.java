package cz.stavbau.backend.features.companies.api;

import cz.stavbau.backend.features.companies.dto.CompanyRegistrationRequest;
import cz.stavbau.backend.features.companies.dto.CompanyRegistrationResponse;
import cz.stavbau.backend.features.companies.service.CompanyRegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants – registrace", description = "Veřejná registrace firmy a vlastníka (OWNER)")
public class CompanyRegistrationController {
    @Autowired
    CompanyRegistrationService service;

    @PostMapping("/register")
    @Operation(summary = "Registrace firmy + OWNER", description = "Public endpoint (bez JWT), vytvoří Company, User a Member(OWNER).")
    @ApiResponse(responseCode = "201", description = "Vytvořeno")
    @ApiResponse(responseCode = "409", description = "Duplicitní IČO nebo e-mail")
    public ResponseEntity<CompanyRegistrationResponse> register(@Valid @RequestBody CompanyRegistrationRequest request) {
        var result = service.register(request);
        return ResponseEntity.status(201).body(result);
    }
}