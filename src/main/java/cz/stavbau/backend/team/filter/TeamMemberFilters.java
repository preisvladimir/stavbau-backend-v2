package cz.stavbau.backend.team.filter;

import cz.stavbau.backend.common.filter.Filters;

/** Normalizace filtrů: trim, empty->null, UPPER pro enum-like hodnoty. */
public final class TeamMemberFilters {
    private TeamMemberFilters() {}

    public static TeamMemberFilter normalize(TeamMemberFilter f) {
        if (f == null) f = new TeamMemberFilter();
        TeamMemberFilter out = new TeamMemberFilter();
        out.setQ(Filters.normQ(f.getQ()));           // trim, empty -> null
        out.setRole(Filters.normUpper(f.getRole())); // trim, empty -> null, UPPER
        out.setStatus(Filters.normUpper(f.getStatus())); // pokud pole existuje
        return out;
    }
}
