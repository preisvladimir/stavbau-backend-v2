package cz.stavbau.backend.common.api.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String countryCode;
    private String countryName;

    private String regionCode;
    private String regionName;
    private String districtCode;
    private String districtName;
    private String municipalityCode;
    private String municipalityName;

    private String city;
    private String cityPart;
    private String street;
    private String houseNumber;
    private String orientationNumber;
    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String formatted;
    private String source; // enum name (USER/ARES/GEO/IMPORT)
}
