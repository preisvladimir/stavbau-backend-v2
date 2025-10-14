package cz.stavbau.backend.features.companies.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class CompanyDto {
    private UUID id;
    private String ico;
    private String obchodniJmeno;

    private String pravniFormaCode;
    private String financniUradCode;

    private LocalDate datumVzniku;
    private LocalDate datumAktualizaceAres;

    private String czNacePrevazujici;


    private List<String> czNace;

    private String zakladniUzemniJednotka;
    private String okresNutsLau;
    private String institucionalniSektor2010;
    private String kategoriePoctuPracovniku;

    private RegisteredAddressDto sidlo;
    // doručovací adresu v DTO zatím neuvádím (Ares ji nevrací); kdykoliv doplníme.

}
