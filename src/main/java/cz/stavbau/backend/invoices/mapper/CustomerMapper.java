// src/main/java/cz/stavbau/backend/invoices/mapper/CustomerMapper.java
package cz.stavbau.backend.invoices.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.common.domain.Address;
import cz.stavbau.backend.common.mapping.AddressMapper;
import cz.stavbau.backend.common.mapping.MapStructCentralConfig;
import cz.stavbau.backend.invoices.dto.CustomerDto;
import cz.stavbau.backend.invoices.dto.CustomerSummaryDto;
import cz.stavbau.backend.invoices.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    config = MapStructCentralConfig.class,
            uses = AddressMapper.class,
            unmappedTargetPolicy = ReportingPolicy.IGNORE
 )
public interface CustomerMapper {

    @Mapping(target = "billingAddressJson", expression = "java( toLegacyJson(src.getBillingAddress()) )")
    CustomerDto toDto(Customer src);

    // typed zápis – preferuj AddressDto; fallback z legacy JSON, pokud přijde
    @Mapping(target = "billingAddress", expression = "java( resolveAddressEntity(dto) )")
    Customer toEntity(CustomerDto dto);

    @Mapping(target = "ico", expression = "java(src.getIco())")
    @Mapping(target = "dic", expression = "java(src.getDic())")
    CustomerSummaryDto toSummaryDto(Customer src);

    // default helper – preferuje typed DTO, fallback na legacy JSON
    default Address resolveAddressEntity(CustomerDto dto) {
        if (dto.getBillingAddress() != null) {
             return org.mapstruct.factory.Mappers
                              .getMapper(cz.stavbau.backend.common.mapping.AddressMapper.class)
                              .toEntity(dto.getBillingAddress());
        }
        if (dto.getBillingAddressJson() != null && !dto.getBillingAddressJson().isBlank()) {
            try {
                return new ObjectMapper().readValue(dto.getBillingAddressJson(), Address.class);
            } catch (Exception ignored) { /* necháme null */ }
        }
        return null;
    }
    // Serializace typed adresy do legacy stringu pro dočasnou kompatibilitu FE
    default String toLegacyJson(cz.stavbau.backend.common.domain.Address a) {
        if (a == null) return null;
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(a);
            } catch (Exception e) {
                return null;
            }
    }
}
