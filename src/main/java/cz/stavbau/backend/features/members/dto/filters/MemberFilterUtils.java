package cz.stavbau.backend.features.members.dto.filters;

import cz.stavbau.backend.common.filter.Filters;

/** Normalizace filtrů: trim, empty -> null, UPPER pro enum-like hodnoty. */
public final class MemberFilterUtils {
    private MemberFilterUtils() {}

    public static MemberFilter normalize(MemberFilter in) {
        MemberFilter f = (in == null) ? new MemberFilter() : in;
        MemberFilter out = new MemberFilter();
        out.setQ(Filters.normQ(f.getQ()));                 // trim, empty -> null
        out.setRole(Filters.normUpper(f.getRole()));       // trim, empty -> null, UPPER
        out.setStatus(Filters.normUpper(f.getStatus()));   // trim, empty -> null, UPPER
        return out;
    }
}
