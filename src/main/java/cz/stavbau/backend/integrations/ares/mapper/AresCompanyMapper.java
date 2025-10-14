package cz.stavbau.backend.integrations.ares.mapper;

import cz.stavbau.backend.common.mapping.MapStructCentralConfig;
import cz.stavbau.backend.integrations.ares.dto.AresSubjectDto;
import cz.stavbau.backend.features.companies.model.Company;
import cz.stavbau.backend.features.companies.model.RegisteredAddress;
import org.mapstruct.*;

import java.util.*;

@Mapper(config = MapStructCentralConfig.class)
public interface AresCompanyMapper {

    // -------- LEGACY/ARRAY: AresSubjectDto.Zaznam --------
    @Mapping(target = "defaultLocale", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    // BaseEntity (soft-delete + version)
    @Mapping(target = "deleted",   constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version",   ignore = true)

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
    @Mapping(target = "adresaDorucovaci", ignore = true) // ARES vrací jen textové řádky
    @Mapping(target = "registrace", ignore = true)       // MVP: @Transient v entitě
    @Mapping(target = "aresRaw", ignore = true)          // doplníme v @AfterMapping
    @Mapping(target = "czNace", source = "czNace")
    Company fromLegacy(AresSubjectDto.Zaznam src, @Context Map<String, Object> raw);

    // -------- SINGLE-OBJECT: AresSubjectDto --------
    @Mapping(target = "defaultLocale", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    // BaseEntity (soft-delete + version)
    @Mapping(target = "deleted",   constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version",   ignore = true)

    @Mapping(target = "ico", source = "ico")
    @Mapping(target = "obchodniJmeno", source = "obchodniJmeno")
    @Mapping(target = "pravniFormaCode", source = "pravniForma")
    @Mapping(target = "financniUradCode", source = "financniUrad")
    @Mapping(target = "datumVzniku", source = "datumVzniku")
    @Mapping(target = "datumAktualizaceAres", source = "datumAktualizace")
    @Mapping(target = "aresLastSyncAt", expression = "java(java.time.OffsetDateTime.now())")

    @Mapping(target = "czNacePrevazujici", source = "czNacePrevazujici")
    @Mapping(target = "zakladniUzemniJednotka", ignore = true)
    @Mapping(target = "okresNutsLau", ignore = true)
    @Mapping(target = "institucionalniSektor2010", ignore = true)
    @Mapping(target = "kategoriePoctuPracovniku", ignore = true)

    @Mapping(target = "sidlo", source = "sidlo")
    @Mapping(target = "adresaDorucovaci", ignore = true)
    @Mapping(target = "registrace", ignore = true)
    @Mapping(target = "aresRaw", ignore = true)
    @Mapping(target = "czNace", source = "czNace")
    Company fromSingle(AresSubjectDto src, @Context Map<String, Object> raw);

    // -------- pomocné --------
    RegisteredAddress mapAddress(AresSubjectDto.Sidlo src);

    default Set<String> mapNace(java.util.List<String> list) {
        return list == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(list);
    }

    @AfterMapping
    default void attachRaw(@MappingTarget Company target, @Context Map<String, Object> raw) {
        target.setAresRaw(raw);
    }
}
