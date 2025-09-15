// FILE: tenants/api/CompanyRegistrationController.java
package cz.stavbau.backend.tenants.api;

import cz.stavbau.backend.tenants.dto.CompanyRegistrationRequest;
import cz.stavbau.backend.tenants.dto.CompanyRegistrationResponse;
import cz.stavbau.backend.tenants.service.CompanyRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies")
public class CompanyRegistrationController {

    private final CompanyRegistrationService service;

    @PostMapping("/register")
    public ResponseEntity<CompanyRegistrationResponse> register(
            @RequestBody @Valid CompanyRegistrationRequest request
    ) {
        var result = service.register(request);
        return ResponseEntity.status(201).body(result);
    }
}
