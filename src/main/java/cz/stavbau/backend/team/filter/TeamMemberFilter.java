package cz.stavbau.backend.team.filter;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeamMemberFilter {
    /** Fulltext přes user.Email, member.firstName/lastName, phone */
    private String q;
    /** Filtr role na CompanyMember.companyRole (string/enum dle tvého modelu) */
    private String role;
}
