package cz.stavbau.backend.common.mapping;

import cz.stavbau.backend.common.api.dto.AddressDto;
import cz.stavbau.backend.common.domain.Address;
import cz.stavbau.backend.common.domain.AddressSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        config = MapStructCentralConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = { cz.stavbau.backend.common.domain.AddressSource.class }
        )
public interface AddressMapper {

    @Mapping(target = "source", expression = "java( src.getSource() == null ? null : src.getSource().name() )")
    AddressDto toDto(Address src);

    @Mapping(target = "source", expression = "java( dto.getSource() == null ? null : AddressSource.valueOf(dto.getSource()) )")
    @Mapping(target = "raw", ignore = true) // DTO neobsahuje raw
    Address toEntity(AddressDto dto);
}
