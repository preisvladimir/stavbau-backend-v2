package cz.stavbau.backend.tenants.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.integrations.ares.AresService;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.tenants.dto.CompanyDto;
import cz.stavbau.backend.tenants.mapping.CompanyMapper;
import cz.stavbau.backend.tenants.model.Company;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CompanyService {

    private final AresService aresService;
    private final CompanyMapper companyMapper;
    private final ObjectMapper objectMapper;

    public CompanyService(AresService aresService, CompanyMapper companyMapper, ObjectMapper objectMapper) {
        this.aresService = aresService;
        this.companyMapper = companyMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Načte firmu z ARES podle IČO a vrátí CompanyDto připravené pro FE formulář.
     * (MVP: neukládá do DB, jen předvyplnění — uložení přidáme v dalším kroku.)
     */
    @Transactional(readOnly = true)
    public CompanyDto lookupByAres(String ico) {
        // 1) Získáme ARES odpověď (DTO)
        AresSubjectDto response = aresService.fetchRaw(ico);
        if (response == null || response.getZaznamy() == null || response.getZaznamy().isEmpty()) {
            return null;
        }
        AresSubjectDto.Zaznam zaznam = response.getZaznamy().get(0);

        // 2) Připravíme raw snapshot pro aresRaw (JSONB)
        Map<String, Object> raw = objectMapper.convertValue(response, new TypeReference<Map<String, Object>>() {});

        // 3) Přemapujeme ARES -> Company (entita v paměti), včetně aresRaw přes @Context
        Company company = aresService.mapToCompany(zaznam, raw);

        // 4) Převod na DTO pro FE
        return companyMapper.toDto(company);
    }
}
