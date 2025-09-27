package cz.stavbau.backend.common.persistence;

import cz.stavbau.backend.common.domain.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Jednoduchý Jackson konvertor pro JSONB sloupce.
 * V MVP použije default ObjectMapper (lokální instance).
 * Později lze nahradit injektovaným mapperem dle JacksonConfig.
 */
@Converter(autoApply = false)
public class AddressJsonConverter implements AttributeConverter<Address, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Address attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Address → JSON convert failed", e);
        }
    }

    @Override
    public Address convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return MAPPER.readValue(dbData, Address.class);
        } catch (Exception e) {
            throw new IllegalStateException("JSON → Address convert failed", e);
        }
    }
}
