package cz.stavbau.backend.integrations.ares.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pro odpověď z ARES API (ekonomické subjekty).
 * Odpovídá struktuře JSON:
 * {
 *   "ico": "...",
 *   "obchodniJmeno": "...",
 *   "sidlo": { ... },
 *   "pravniForma": "...",
 *   ...
 * }
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AresSubjectDto {


    private String icoId;
    private List<Zaznam> zaznamy;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Zaznam {
        private String ico;
        private String obchodniJmeno;
        private Sidlo sidlo;
        private String pravniForma;
        private String financniUrad;
        private LocalDate datumVzniku;
        private LocalDate datumAktualizace;
        private String pravniFormaRos;
        private List<String> czNace;
        private String czNacePrevazujici;
        private StatistickeUdaje statistickeUdaje;
        private String zakladniUzemniJednotka;
        private Boolean primarniZaznam;
        private String okresNutsLau;
    }

    // -----------------------------
    // Nested classes
    // -----------------------------

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Sidlo {
        private String kodStatu;
        private String nazevStatu;
        private String kodKraje;
        private String nazevKraje;
        private String kodOkresu;
        private String nazevOkresu;
        private String kodObce;
        private String nazevObce;
        private String kodUlice;
        private String nazevUlice;
        private String cisloDomovni;
        private String kodCastiObce;
        private String nazevCastiObce;
        private String kodAdresnihoMista;
        private String psc;
        private String textovaAdresa;
        private Boolean standardizaceAdresy;
        private String typCisloDomovni;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatistickeUdaje {
        private String institucionalniSektor2010;
        private String kategoriePoctuPracovniku;
    }
}
