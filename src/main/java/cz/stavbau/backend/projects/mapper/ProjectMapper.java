package cz.stavbau.backend.projects.mapper;

import cz.stavbau.backend.projects.dto.*;
import cz.stavbau.backend.projects.model.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(config = cz.stavbau.backend.common.mapping.MapStructCentralConfig.class)
public interface ProjectMapper {

    // Entity -> DTO (detail)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "statusLabel", ignore = true)
    ProjectDto toDto(Project entity);

    // Entity -> DTO (summary)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "statusLabel", ignore = true)
    ProjectSummaryDto toSummary(Project entity);

    List<ProjectSummaryDto> toSummaryList(List<Project> entities);

    // Create -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true) // doplní service
    @Mapping(target = "status", constant = "PLANNED")
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "siteAddressJson", ignore = true) // přijde v dalším PR
    @Mapping(target = "tags", ignore = true)
    Project fromCreate(CreateProjectRequest req);

    // Update (do exist entity)
    void update(@MappingTarget Project entity, UpdateProjectRequest req);
}
