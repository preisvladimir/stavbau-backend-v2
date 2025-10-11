// src/main/java/cz/stavbau/backend/team/mapping/MemberMapper.java
package cz.stavbau.backend.team.mapping;

import cz.stavbau.backend.team.api.dto.MemberDto;
import cz.stavbau.backend.team.dto.TeamSummaryDto;
import cz.stavbau.backend.tenants.membership.model.CompanyMember;
import cz.stavbau.backend.users.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    // ========== DETAIL (MemberDto) ==========
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "userId",   source = "user.id")
    @Mapping(target = "email",    source = "user.email")
    @Mapping(target = "state",    source = "user.state")

    // null-safe role (z CompanyMember)
    @Mapping(target = "role",     expression = "java(member.getRole() != null ? member.getRole().name() : null)")

    // Jméno/telefon z CompanyMember (User je read-only, nemá je mít)
    @Mapping(target = "firstName", source = "member.firstName")
    @Mapping(target = "lastName",  source = "member.lastName")
    @Mapping(target = "phone",     source = "member.phone")

    // status přichází zvenčí (např. vypočtený service vrstvou)
    @Mapping(target = "status",    source = "status")
    MemberDto toDto(User user, CompanyMember member, String status);


    // ========== SUMMARY (TeamSummaryDto) ==========

    /**
     * Preferované mapování přes CompanyMember → díky read-only referenci na User
     * (member.user) umíme vzít e-mail a userId, pokud je přifetchnuté.
     * V opačném případě můžeš použít overload s User parametrem níže.
     */
    @Mapping(target = "id",        source = "id")
    @Mapping(target = "userId",    source = "user.id")
    @Mapping(target = "email",     source = "user.email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName",  source = "lastName")
    @Mapping(target = "phone",     source = "phone")
    @Mapping(target = "companyRole", expression = "java(member.getRole() != null ? member.getRole().name() : null)")
    // kompat alias pro starší FE – stejné jako companyRole
    @Mapping(target = "role",        expression = "java(member.getRole() != null ? member.getRole().name() : null)")
    @Mapping(target = "status",      source = "user.state") // pokud CompanyMember status nemá, můžeš posílat null
    @Mapping(target = "createdAt",   source = "createdAt")
    @Mapping(target = "updatedAt",   source = "updatedAt")
    // displayName: First Last | email | null
    @Mapping(target = "displayName", expression = "java(_displayName(member))")
    TeamSummaryDto toSummaryDto(CompanyMember member);

  //  @IterableMapping(qualifiedByName = "toSummaryDto")
 //   List<TeamSummaryDto> toSummaryList(List<CompanyMember> members);


    /**
     * Alternativní overload, pokud máš `User` bokem a nechceš spoléhat na `member.getUser()`.
     */
    @Mapping(target = "id",        source = "member.id")
    @Mapping(target = "userId",    source = "user.id")
    @Mapping(target = "email",     source = "user.email")
    @Mapping(target = "firstName", source = "member.firstName")
    @Mapping(target = "lastName",  source = "member.lastName")
    @Mapping(target = "phone",     source = "member.phone")
    @Mapping(target = "companyRole", expression = "java(member.getRole() != null ? member.getRole().name() : null)")
    @Mapping(target = "role",        expression = "java(member.getRole() != null ? member.getRole().name() : null)")
    @Mapping(target = "status",      source = "status")
    @Mapping(target = "createdAt",   source = "member.createdAt")
    @Mapping(target = "updatedAt",   source = "member.updatedAt")
    @Mapping(target = "displayName", expression = "java(_displayName(member, user))")
    TeamSummaryDto toSummary(User user, CompanyMember member, String status);


    // ========== Helpers (MapStruct default methods) ==========

    /** displayName s fallbackem na e-mail: "First Last" | email | null */
    default String _displayName(CompanyMember member) {
        return _displayName(member, member != null ? member.getUser() : null);
    }

    default String _displayName(CompanyMember member, User user) {
        String first = member != null ? trimOrNull(member.getFirstName()) : null;
        String last  = member != null ? trimOrNull(member.getLastName())  : null;
        String email = user != null ? trimOrNull(user.getEmail()) : null;

        String full = joinNonBlank(first, last);
        return !isBlank(full) ? full : email;
    }

    // --- tiny utils ---
    default boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    default String trimOrNull(String s) { return s == null ? null : s.trim(); }
    default String joinNonBlank(String a, String b) {
        if (isBlank(a) && isBlank(b)) return null;
        if (isBlank(a)) return b.trim();
        if (isBlank(b)) return a.trim();
        return (a.trim() + " " + b.trim()).trim();
    }
}
