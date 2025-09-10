package cz.stavbau.backend.tenants.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisteredAddressDto {
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
