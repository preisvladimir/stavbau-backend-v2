package cz.stavbau.backend.projects.mapper;

import cz.stavbau.backend.common.mapping.AddressMapper;
import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.model.Project;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        config = cz.stavbau.backend.common.mapping.MapStructCentralConfig.class,
        uses = AddressMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface ProjectMapper {

    // ========= Entity -> DTO (detail) =========
    @Mapping(target = "name", ignore = true)          // doplní service z translations
    @Mapping(target = "description", ignore = true)   // doplní service z translations
    @Mapping(target = "statusLabel", ignore = true)   // doplní service (EnumLabeler)
    ProjectDto toDto(Project entity);

    // ========= Entity -> DTO (summary) =========
    @Mapping(target = "name", ignore = true)          // doplní service z translations
    @Mapping(target = "statusLabel", ignore = true)   // doplní service (EnumLabeler)
    ProjectSummaryDto toSummary(Project entity);

    List<ProjectSummaryDto> toSummaryList(List<Project> entities);

    // ========= Create -> Entity =========
    // - implicitní mapování shodných názvů (code, customerId, projectManagerId, plannedStartDate, plannedEndDate, currency, vatMode)
    // - explicitně ignorujeme všechno, co teď neplníme (audit, tenancy, actual*, archived, siteAddressJson, tags)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)       // doplní service
    @Mapping(target = "status", constant = "PLANNED")   // výchozí stav
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "siteAddress", ignore = true) // plní ProjectService (typed JSONB přes AddressMapper)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "actualStartDate", ignore = true)
    @Mapping(target = "actualEndDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Project fromCreate(CreateProjectRequest req);

    // ========= Update (do exist entity) =========
    // - implicitní mapování shodných názvů
    // - null hodnoty ve requestu nezahladí existující data (IGNORE)
    // - ostatní pole ignorujeme
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "status", ignore = true)          // status budeme řídit separátně (workflow)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "siteAddress", ignore = true)    // plní ProjectService při PATCH, jen pokud přišlo v requestu
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "actualStartDate", ignore = true)
    @Mapping(target = "actualEndDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void update(@MappingTarget Project entity, UpdateProjectRequest req);
}
