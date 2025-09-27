package cz.stavbau.backend.common.domain;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    // ISO 3166-1 alpha-2 (např. "CZ")
    private String countryCode;
    private String countryName;

    // Volitelné hierarchie (CZ: kraj/okres/obec/část)
    private String regionCode;
    private String regionName;
    private String districtCode;
    private String districtName;
    private String municipalityCode;
    private String municipalityName;

    // Město/Část města/ulice/čísla
    private String city;
    private String cityPart;
    private String street;
    private String houseNumber;        // popisné/evidenční
    private String orientationNumber;  // orientační
    private String postalCode;         // PSČ

    // Geo
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Uživatelsky čitelné
    private String formatted;

    // Zdroj (ARES, GEO, USER, IMPORT)
    private AddressSource source;

    // Volitelně surová data poskytovatele (nebude mapováno do DTO – interní diagnostika)
    private Map<String, Object> raw;
}
