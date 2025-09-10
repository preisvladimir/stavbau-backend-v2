package cz.stavbau.backend.tenants.mapping;

import cz.stavbau.backend.common.mapping.MapStructCentralConfig;
import cz.stavbau.backend.tenants.dto.CompanyDto;
import cz.stavbau.backend.tenants.dto.RegisteredAddressDto;
import cz.stavbau.backend.tenants.model.Company;
import cz.stavbau.backend.tenants.model.RegisteredAddress;
import org.mapstruct.*;

@Mapper(config = MapStructCentralConfig.class)
public interface CompanyMapper {

    // Entity -> DTO
    @Mapping(target = "czNace", expression = "java(new java.util.ArrayList<>(entity.getCzNace()))")
    CompanyDto toDto(Company entity);

    RegisteredAddressDto toDto(RegisteredAddress addr);

    // DTO -> Entity
    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "id", ignore = true)                 // JPA generuje
    @Mapping(target = "createdAt", ignore = true)          // auditing spravuje Spring
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "aresRaw", ignore = true)            // snapshot si řeší ARES vrstva
    @Mapping(target = "adresaDorucovaci", ignore = true)   // ARES ji nevrací
    @Mapping(target = "registrace", ignore = true)         // zatím nepoužíváme
    @Mapping(target = "aresLastSyncAt", ignore = true)     // nastavuje ARES mapper
    @Mapping(target = "czNace", expression =
            "java(dto.getCzNace() == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(dto.getCzNace()))")
    Company toEntity(CompanyDto dto);

    @InheritInverseConfiguration(name = "toDto")
    RegisteredAddress toEntity(RegisteredAddressDto dto);
}
