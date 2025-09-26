// src/main/java/cz/stavbau/backend/invoices/mapper/CustomerMapper.java
package cz.stavbau.backend.invoices.mapper;

import cz.stavbau.backend.invoices.dto.CustomerDto;
import cz.stavbau.backend.invoices.dto.CustomerSummaryDto;
import cz.stavbau.backend.invoices.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        config = cz.stavbau.backend.common.mapping.MapStructCentralConfig.class
)
public interface CustomerMapper {

    CustomerDto toDto(Customer src);

    @Mapping(target = "ico", expression = "java(src.getIco())")
    @Mapping(target = "dic", expression = "java(src.getDic())")
    CustomerSummaryDto toSummaryDto(Customer src);
}
