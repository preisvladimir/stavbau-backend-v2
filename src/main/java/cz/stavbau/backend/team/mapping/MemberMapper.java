package cz.stavbau.backend.team.mapping;

import cz.stavbau.backend.team.api.dto.MemberDto;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "userId",   source = "user.id")
    @Mapping(target = "email",    source = "user.email")
    @Mapping(target = "role",     expression = "java(member.getCompanyRole().name())")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName",  source = "user.lastName")
    @Mapping(target = "phone",     source = "user.phone")
    @Mapping(target = "status",    source = "status")
    MemberDto toDto(User user, CompanyMember member, String status);
}
