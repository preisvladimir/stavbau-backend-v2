package cz.stavbau.backend.features.registrationV1.api;

import cz.stavbau.backend.features.registrationV1.dto.RegistrationRequestV1;
import cz.stavbau.backend.features.registrationV1.dto.RegistrationResponseV1;
import cz.stavbau.backend.features.registrationV1.service.RegistrationServiceV1;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Tenants – registrace", description = "Veřejná registrace firmy a vlastníka (OWNER)")
public class RegistrationControllerV1 {
    @Autowired
    RegistrationServiceV1 service;

    @PostMapping("/register")
    @Operation(summary = "Registrace firmy + OWNER", description = "Public endpoint (bez JWT), vytvoří Company, User a Member(OWNER).")
    @ApiResponse(responseCode = "201", description = "Vytvořeno")
    @ApiResponse(responseCode = "409", description = "Duplicitní IČO nebo e-mail")
    public ResponseEntity<RegistrationResponseV1> register(@Valid @RequestBody RegistrationRequestV1 request) {
        var result = service.register(request);
        return ResponseEntity.status(201).body(result);
    }
}