 package cz.stavbau.backend.tenants.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Embeddable
public class RegisteredAddress {

    @Column(length = 2)
    private String kodStatu;

    @Column(length = 64)
    private String nazevStatu;

    @Column(length = 4)
    private String kodKraje;

    @Column(length = 64)
    private String nazevKraje;

    @Column(length = 8)
    private String kodOkresu;

    @Column(length = 64)
    private String nazevOkresu;

    @Column(length = 12)
    private String kodObce;

    @Column(length = 128)
    private String nazevObce;

    @Column(length = 16)
    private String kodUlice;

    @Column(length = 128)
    private String nazevUlice;

    /** Číslo domovní – držíme jako String kvůli formátu. */
    @Column(length = 16)
    private String cisloDomovni;

    @Column(length = 12)
    private String kodCastiObce;

    @Column(length = 128)
    private String nazevCastiObce;

    @Column(length = 16)
    private String kodAdresnihoMista;

    /** PSČ jako String (5), ale necháváme delší rezervu kvůli variantám. */
    @Column(length = 16)
    private String psc;

    @Column(length = 512)
    private String textovaAdresa;

    /** 1/0 v ARES, ale v doméně boolean. */
    private Boolean standardizaceAdresy;

    /** `typCisloDomovni` – pokud potřebujeme, držme jako String pro kompatibilitu. */
    @Column(length = 4)
    private String typCisloDomovni;
}
