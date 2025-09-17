package cz.stavbau.backend.tenants.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.integrations.ares.AresService;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.tenants.dto.CompanyDto;
import cz.stavbau.backend.tenants.mapping.AresCompanyMapper;
import cz.stavbau.backend.tenants.mapping.CompanyMapper;
import cz.stavbau.backend.tenants.model.Company;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CompanyService {

    private final AresService aresService;
    private final CompanyMapper companyMapper;
    private final AresCompanyMapper aresCompanyMapper;
    private final ObjectMapper objectMapper;

    public CompanyService(AresService aresService, CompanyMapper companyMapper, AresCompanyMapper aresCompanyMapper, ObjectMapper objectMapper) {
        this.aresService = aresService;
        this.companyMapper = companyMapper;
        this.aresCompanyMapper = aresCompanyMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Načte firmu z ARES podle IČO a vrátí CompanyDto připravené pro FE formulář.
     * (MVP: neukládá do DB, jen předvyplnění — uložení přidáme v dalším kroku.)
     */
    @Transactional(readOnly = true)
    public CompanyDto lookupByAres(String ico) {

        AresSubjectDto resp = aresService.fetchRaw(ico);
        if (resp == null) return null;

        Map<String,Object> raw = objectMapper.convertValue(resp, new TypeReference<Map<String,Object>>() {});
        Company company;

        if (resp.getZaznamy() != null && !resp.getZaznamy().isEmpty()) {
            company = aresCompanyMapper.fromLegacy(resp.getZaznamy().get(0), raw);
        } else {
            company = aresCompanyMapper.fromSingle(resp, raw);
        }
        return companyMapper.toDto(company);
    }

}
