package cz.stavbau.backend.integrations.ares;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.integrations.ares.mapper.AresCompanyMapper;
import cz.stavbau.backend.features.companies.model.Company;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AresService {

    private final AresClient client;
    private final AresCompanyMapper mapper;
    private final ObjectMapper objectMapper;

    public AresService(AresClient client, AresCompanyMapper mapper, ObjectMapper objectMapper) {
        this.client = client;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

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

}
