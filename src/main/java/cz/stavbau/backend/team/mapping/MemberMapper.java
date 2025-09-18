package cz.stavbau.backend.team.mapping;

import cz.stavbau.backend.team.api.dto.MemberDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    // TODO(PR 2/N): mapování z User + CompanyMember na MemberDto
    // MemberDto toDto(User user, CompanyMember member, String status);
}
