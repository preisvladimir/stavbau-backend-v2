package cz.stavbau.backend.integrations.ares.mapper;

import cz.stavbau.backend.common.mapping.MapStructCentralConfig;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.tenants.model.Company;
import cz.stavbau.backend.tenants.model.RegisteredAddress;
import org.mapstruct.*;

import java.util.Map;

@Mapper(config = MapStructCentralConfig.class)
public interface AresCompanyMapper {

    @Mapping(target = "id", ignore = true)                 // JPA generuje
    @Mapping(target = "createdAt", ignore = true)          // auditing spravuje Spring
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)

    @Mapping(target = "ico", source = "ico")
    @Mapping(target = "obchodniJmeno", source = "obchodniJmeno")
    @Mapping(target = "pravniFormaCode", source = "pravniForma")
    @Mapping(target = "financniUradCode", source = "financniUrad")
    @Mapping(target = "datumVzniku", source = "datumVzniku")
    @Mapping(target = "datumAktualizaceAres", source = "datumAktualizace")
    @Mapping(target = "aresLastSyncAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "czNacePrevazujici", source = "czNacePrevazujici")
    @Mapping(target = "zakladniUzemniJednotka", source = "zakladniUzemniJednotka")
    @Mapping(target = "okresNutsLau", source = "okresNutsLau")
    @Mapping(target = "institucionalniSektor2010", source = "statistickeUdaje.institucionalniSektor2010")
    @Mapping(target = "kategoriePoctuPracovniku", source = "statistickeUdaje.kategoriePoctuPracovniku")
    @Mapping(target = "sidlo", source = "sidlo")
    @Mapping(target = "adresaDorucovaci", ignore = true)   // ARES ji nevrací
    @Mapping(target = "registrace", ignore = true)         // zatím nepoužíváme
    @Mapping(target = "aresRaw", ignore = true)            // doplníme AfterMapping
    @Mapping(target = "czNace", source = "czNace")
    Company toEntity(AresSubjectDto.Zaznam src, @Context Map<String, Object> raw);

    RegisteredAddress mapAddress(AresSubjectDto.Sidlo src);

    default java.util.Set<String> mapNace(java.util.List<String> list) {
        return list == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(list);
    }

    @AfterMapping
    default void attachRaw(@MappingTarget Company target, @Context Map<String, Object> raw) {
        target.setAresRaw(raw);
    }
}
