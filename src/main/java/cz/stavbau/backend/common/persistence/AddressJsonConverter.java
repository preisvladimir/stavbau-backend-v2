package cz.stavbau.backend.common.persistence;

import cz.stavbau.backend.common.domain.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;
import java.sql.SQLException;

/**
 * Jednoduchý Jackson konvertor pro JSONB sloupce.
 * V MVP použije default ObjectMapper (lokální instance).
 * Později lze nahradit injektovaným mapperem dle JacksonConfig.
 */
@Converter(autoApply = false)
public class AddressJsonConverter implements AttributeConverter<Address, PGobject> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public PGobject convertToDatabaseColumn(Address attribute) {
        if (attribute == null) return null;
           try {
               var jsonb = new PGobject();
               jsonb.setType("jsonb");
               jsonb.setValue(MAPPER.writeValueAsString(attribute));
               return jsonb;
           } catch (JsonProcessingException | SQLException e) {
               throw new IllegalStateException("Address → JSONB convert failed", e);
           }
    }

    @Override
    public Address convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null || dbData.getValue().isBlank()) return null;
        try {
            return MAPPER.readValue(dbData.getValue(), Address.class);
        } catch (Exception e) {
            throw new IllegalStateException("JSON → Address convert failed", e);
        }
    }
}
