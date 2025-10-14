package cz.stavbau.backend.features.companies.mapper;

import cz.stavbau.backend.common.mapping.MapStructCentralConfig;
import cz.stavbau.backend.features.companies.dto.CompanyDto;
import cz.stavbau.backend.features.companies.dto.RegisteredAddressDto;
import cz.stavbau.backend.features.companies.model.Company;
import cz.stavbau.backend.features.companies.model.RegisteredAddress;
import org.mapstruct.*;

@Mapper(
        config = MapStructCentralConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface CompanyMapper {

    // Entity -> DTO (defaultLocale se namapuje automaticky by-name, pokud je v CompanyDto)
    @Mapping(target = "czNace", expression = "java(new java.util.ArrayList<>(entity.getCzNace()))")
    CompanyDto toDto(Company entity);

    RegisteredAddressDto toDto(RegisteredAddress addr);

    // DTO -> Entity
    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "defaultLocale", ignore = true) // mapovat až po doplnění do Create/Update DTO + UI
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true) @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true) @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "aresRaw", ignore = true)
    @Mapping(target = "adresaDorucovaci", ignore = true)
    @Mapping(target = "registrace", ignore = true)
    @Mapping(target = "aresLastSyncAt", ignore = true)
    @Mapping(target = "czNace", expression =
            "java(dto.getCzNace() == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(dto.getCzNace()))")
    Company toEntity(CompanyDto dto);

    @InheritInverseConfiguration(name = "toDto")
    RegisteredAddress toEntity(RegisteredAddressDto dto);
}
