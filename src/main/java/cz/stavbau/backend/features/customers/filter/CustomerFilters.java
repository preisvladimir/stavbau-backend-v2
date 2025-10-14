package cz.stavbau.backend.features.customers.filter;

import cz.stavbau.backend.common.filter.Filters;

public final class CustomerFilters {
    private CustomerFilters() {}

    public static CustomerFilter normalize(CustomerFilter in) {
        CustomerFilter f = (in == null) ? new CustomerFilter() : in;
        CustomerFilter out = new CustomerFilter();
        // 1) stringy
        out.setQ(Filters.normQ(f.getQ()));
        // type: trim+upper → null if blank
        out.setType(Filters.upper(in != null ? f.getType() : null));

        out.setStatus(f.getStatus());
        return out;
    }
}
