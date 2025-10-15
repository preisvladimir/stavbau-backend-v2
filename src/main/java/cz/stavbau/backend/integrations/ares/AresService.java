package cz.stavbau.backend.integrations.ares;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.features.companies.dto.CompanyDto;
import cz.stavbau.backend.features.companies.mapper.CompanyMapper;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.integrations.ares.mapper.AresCompanyMapper;
import cz.stavbau.backend.features.companies.model.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AresService {

    private final AresClient client;
    private final AresCompanyMapper mapper;
    private final ObjectMapper objectMapper;
    private final CompanyMapper companyMapper;

    public AresSubjectDto fetchRaw(String ico) {
        return client.fetchByIco(ico);
    }

    /** Legacy větev – když pracuješ s AresSubjectDto.Zaznam (z pole zaznamy[]) */
    public Company mapToCompany(AresSubjectDto.Zaznam record, Map<String, Object> raw) {
        return mapper.fromLegacy(record, raw);   // dříve: mapper.toEntity(...)
    }

    /** Single-object větev – když máš rovnou root payload (aktuální ARES odpověď) */
    public Company mapToCompany(AresSubjectDto response, Map<String, Object> raw) {
        return mapper.fromSingle(response, raw);
    }

    /**
     * Načte firmu z ARES podle IČO a vrátí CompanyDto připravené pro FE formulář.
     * (MVP: neukládá do DB, jen předvyplnění — uložení přidáme v dalším kroku.)
     */
    @Transactional(readOnly = true)
    public CompanyDto lookupByIco(String ico) {

        AresSubjectDto resp = fetchRaw(ico);
        if (resp == null) return null;

        Map<String,Object> raw = objectMapper.convertValue(resp, new TypeReference<Map<String,Object>>() {});
        Company company;

        if (resp.getZaznamy() != null && !resp.getZaznamy().isEmpty()) {
            company = mapper.fromLegacy(resp.getZaznamy().get(0), raw);
        } else {
            company = mapper.fromSingle(resp, raw);
        }
        return companyMapper.toDto(company);
    }

}
