// src/main/java/cz/stavbau/backend/team/mapping/MemberMapper.java
package cz.stavbau.backend.features.members.mapper;

import cz.stavbau.backend.features.members.dto.read.MemberDto;
import cz.stavbau.backend.features.members.dto.read.MemberSummaryDto;
import cz.stavbau.backend.features.members.model.Member;
import cz.stavbau.backend.identity.users.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    // ========== DETAIL (MemberDto) ==========
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "userId",   source = "user.id")
    @Mapping(target = "email",    source = "user.email")
    @Mapping(target = "state",    source = "user.state")

    // null-safe role (z Member)
    @Mapping(target = "role",     expression = "java(member.getRole() != null ? member.getRole().name() : null)")

    // Jméno/telefon z Member (User je read-only, nemá je mít)
    @Mapping(target = "firstName", source = "member.firstName")
    @Mapping(target = "lastName",  source = "member.lastName")
    @Mapping(target = "phone",     source = "member.phone")

    // status přichází zvenčí (např. vypočtený service vrstvou)
    @Mapping(target = "status",    source = "status")
    MemberDto toDto(User user, Member member, String status);


    // ========== SUMMARY (MemberSummaryDto) ==========

    /**
     * Preferované mapování přes Member → díky read-only referenci na User
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
    @Mapping(target = "status",      source = "user.state") // pokud Member status nemá, můžeš posílat null
    @Mapping(target = "createdAt",   source = "createdAt")
    @Mapping(target = "updatedAt",   source = "updatedAt")
    // displayName: First Last | email | null
    @Mapping(target = "displayName", expression = "java(_displayName(member))")
    MemberSummaryDto toSummaryDto(Member member);

  //  @IterableMapping(qualifiedByName = "toSummaryDto")
 //   List<MemberSummaryDto> toSummaryList(List<Member> members);


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
    MemberSummaryDto toSummary(User user, Member member, String status);


    // ========== Helpers (MapStruct default methods) ==========

    /** displayName s fallbackem na e-mail: "First Last" | email | null */
    default String _displayName(Member member) {
        return _displayName(member, member != null ? member.getUser() : null);
    }

    default String _displayName(Member member, User user) {
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
