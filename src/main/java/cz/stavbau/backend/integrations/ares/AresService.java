package cz.stavbau.backend.integrations.ares;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDtoOld;
import cz.stavbau.backend.tenants.mapping.AresCompanyMapper;
import cz.stavbau.backend.tenants.model.Company;
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

    public AresSubjectDtoOld fetchRawOld(String ico) {
        return client.fetchByIcoOld(ico);
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
