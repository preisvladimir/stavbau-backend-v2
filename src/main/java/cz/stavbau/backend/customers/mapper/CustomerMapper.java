// src/main/java/cz/stavbau/backend/invoices/mapper/CustomerMapper.java
package cz.stavbau.backend.customers.mapper;

import cz.stavbau.backend.common.mapping.AddressMapper;
import cz.stavbau.backend.common.mapping.MapStructCentralConfig;
import cz.stavbau.backend.customers.dto.CustomerDto;
import cz.stavbau.backend.customers.dto.CustomerSummaryDto;
import cz.stavbau.backend.customers.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    config = MapStructCentralConfig.class,
            uses = AddressMapper.class,
            unmappedTargetPolicy = ReportingPolicy.IGNORE
 )
public interface CustomerMapper {

    CustomerDto toDto(Customer src);

    Customer toEntity(CustomerDto dto);

    @Mapping(target = "ico", expression = "java(src.getIco())")
    @Mapping(target = "dic", expression = "java(src.getDic())")
    CustomerSummaryDto toSummaryDto(Customer src);

}
