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

    // ===== Entity -> DTO (detail) =====
    @Mapping(target = "nameLocalized",        ignore = true)
    @Mapping(target = "descriptionLocalized", ignore = true)
    ProjectDto toDto(Project entity);

    // ===== Entity -> DTO (summary) =====
    @Mapping(target = "nameLocalized", ignore = true)
    ProjectSummaryDto toSummaryDto(Project entity);
    List<ProjectSummaryDto> toSummaryList(List<Project> entities);

    // ===== Create -> Entity =====
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)           // generuje service
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "status", constant = "PLANNED")
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "siteAddress", ignore = true)    // service setne pokud přijde
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "actualStartDate", ignore = true)
    @Mapping(target = "actualEndDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Project fromCreate(CreateProjectRequest req);

    // ===== Update -> existing entity =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "status", ignore = true)         // workflow zvlášť
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "siteAddress", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "actualStartDate", ignore = true)
    @Mapping(target = "actualEndDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void update(@MappingTarget Project entity, UpdateProjectRequest req);
}
