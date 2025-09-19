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

    // role vracíme jako skutečnou company roli (OWNER/COMPANY_ADMIN/VIEWER…)
    @Mapping(target = "role",     expression = "java(member.getRole().name())")

    // ↓ změna: bereme z CompanyMember (User to nemá mít)
    @Mapping(target = "firstName", source = "member.firstName")
    @Mapping(target = "lastName",  source = "member.lastName")
    @Mapping(target = "phone",     source = "member.phone")

    @Mapping(target = "status",    source = "status")
    MemberDto toDto(User user, CompanyMember member, String status);
}
