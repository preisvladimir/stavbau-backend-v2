// src/main/java/cz/stavbau/backend/common/mapping/MapStructCentralConfig.java
package cz.stavbau.backend.common.mapping;

import org.mapstruct.*;

@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        builder = @Builder(disableBuilder = true),          // MapStruct nepoužije Lombok builder
        unmappedSourcePolicy = ReportingPolicy.IGNORE,      // auditní pole v entity/DTO nebudou shazovat build
        unmappedTargetPolicy = ReportingPolicy.ERROR        // naopak chraň cílové vlastnosti (přísné)
)
public interface MapStructCentralConfig {}
