package cz.stavbau.backend.features.customers.filter;

import lombok.*;

@Getter @Setter
@ToString(onlyExplicitlyIncluded = true)
public class CustomerFilter {
    /** Fulltext: name, email, phone, ICO, DIC */
    @ToString.Include private String q;

    /** Např. B2B/B2C apod. – pokud používáš */
    @ToString.Include private String type;

    /** Stav/flag (pokud používáš) */
    @ToString.Include private String status;
}
