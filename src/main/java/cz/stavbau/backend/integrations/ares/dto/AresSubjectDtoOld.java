package cz.stavbau.backend.integrations.ares.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AresSubjectDtoOld {
    private String ico;
    private String obchodniJmeno;
    private Sidlo sidlo;
    private String pravniForma;        // kód (např. "101")
    private String financniUrad;       // kód (např. "057")
    private LocalDate datumVzniku;
    private LocalDate datumAktualizace;
    private String primarniZdroj;      // res, rzp, vr, ...
    private AdresaDorucovaci adresaDorucovaci;
    private SeznamRegistraci seznamRegistraci;
    private List<String> czNace;

    @Data
    public static class Sidlo {
        private String kodStatu;
        private String nazevStatu;
        private Integer kodKraje;
        private String nazevKraje;
        private Integer kodOkresu;
        private String nazevOkresu;
        private Integer kodObce;
        private String nazevObce;
        private Integer kodUlice;
        private String nazevUlice;
        private Integer cisloDomovni;     // ARES vrací číslo, uložíme jako String v mapperu
        private Integer kodCastiObce;
        private String nazevCastiObce;
        private Long kodAdresnihoMista;
        private Integer psc;              // ARES někdy číslem; uložíme jako String v mapperu
        private String textovaAdresa;
        private Boolean standardizaceAdresy;
        private Integer typCisloDomovni;
    }

    @Data
    public static class AdresaDorucovaci {
        private String radekAdresy1;
        private String radekAdresy2;
    }

    @Data
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
}
