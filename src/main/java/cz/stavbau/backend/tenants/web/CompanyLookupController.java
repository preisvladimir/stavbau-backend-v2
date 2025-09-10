package cz.stavbau.backend.tenants.web;

import cz.stavbau.backend.tenants.dto.CompanyDto;
import cz.stavbau.backend.tenants.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/lookup")
public class CompanyLookupController {

    private final CompanyService companyService;

    public CompanyLookupController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/ares")
    public ResponseEntity<CompanyDto> lookupByIco(@RequestParam String ico) {
        CompanyDto dto = companyService.lookupByAres(ico);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
