package cz.stavbau.backend.invoices.filter;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomerFilter {
    private String name;
    private String ico;
    private String dic;
    private String email;

    /** fulltext: rozsekáme na slova a AND-ujeme, uvnitř OR přes vybrané sloupce */
    private String q;

    /** např. pro budoucí archiv / aktivní flagy (MVP nech klidně null) */
    private Boolean active;
}
