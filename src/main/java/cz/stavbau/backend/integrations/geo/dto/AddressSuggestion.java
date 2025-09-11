package cz.stavbau.backend.integrations.geo.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AddressSuggestion {
    String formatted;
    String name;
    String label;
    Double lat;
    Double lon;
    BBox bbox;

    List<RegionItem> regionalStructure;

    String street;
    String houseNumber;
    String municipality;
    String municipalityPart;
    String region;
    String country;
    String countryIsoCode;
    String zip;

    @Value
    @Builder
    public static class BBox {
        Double minLat;
        Double minLon;
        Double maxLat;
        Double maxLon;
    }

    @Value
    @Builder
    public static class RegionItem {
        String type;   // e.g., country, region, district, municipality, quarter, street
        String name;
        String code;   // optional ISO/NUTS code if provided by API
    }
}
