package cz.stavbau.backend.tenants.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompanyPreviewDto {
    String ico;
    String obchodniJmeno;
    String pravniFormaCode;
    String datumVzniku;     // ISO (YYYY-MM-DD) pro jednoduché zobrazení
    Sidlo sidlo;

    @Value @Builder
    public static class Sidlo {
        String textovaAdresa;
        String psc;
        String nazevObce;
        String nazevUlice;
        String cisloDomovni;
    }
}
