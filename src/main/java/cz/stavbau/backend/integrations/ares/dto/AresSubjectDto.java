package cz.stavbau.backend.integrations.ares.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AresSubjectDto {
    // === Single-object response (reálná ARES odpověď) ===
    private String ico;
    private String obchodniJmeno;
    private Sidlo sidlo;
    private String pravniForma;
    private String financniUrad;
    private LocalDate datumVzniku;
    private LocalDate datumAktualizace;
    private List<String> czNace;
    private String czNacePrevazujici;

    private AdresaDorucovaci adresaDorucovaci;     // ARES vrací 2 textové řádky
    private SeznamRegistraci seznamRegistraci;     // zatím neperzistujeme
    private String primarniZdroj;
    private List<Object> dalsiUdaje;               // držíme jen pro raw snapshot

    // === Legacy / alternativní pole, ať jsme kompatibilní ===
    private String icoId;                          // někdy duplicitní
    private List<Zaznam> zaznamy;
    private Zaznam zaznam;

    // ---- nested ----
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Sidlo {
        // necháme String, Jackson čísla -> String umí sám
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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AdresaDorucovaci {
        private String radekAdresy1;
        private String radekAdresy2;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SeznamRegistraci {
        private String stavZdrojeVr;
        private String stavZdrojeRes;
        private String stavZdrojeRzp;
        private String stavZdrojeNrpzs;
        private String stavZdrojeRpsh;
        private String stavZdrojeRcns;
        private String stavZdrojeSzr;
        private String stavZdrojeDph;
        private String stavZdrojeSd;
        private String stavZdrojeIr;
        private String stavZdrojeCeu;
        private String stavZdrojeRs;
        private String stavZdrojeRed;
        private String stavZdrojeMonitor;
    }

    // Ponecháme kvůli kompatibilitě se starším JSONem
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatistickeUdaje {
        private String institucionalniSektor2010;
        private String kategoriePoctuPracovniku;
    }
}
