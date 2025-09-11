package cz.stavbau.backend.integrations.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(name = "AddressSuggestion",
        description = "Návrh adresy/geolokační položky z provideru (Mapy.cz). " +
                "Obsahuje lidsky čitelné pole 'formatted', přesné souřadnice a rozpad adresy.")
public class AddressSuggestion {

    @Schema(description = "Lidsky čitelná adresa (fallback kombinace name+location).",
            example = "Týnská ulička 610/7, Praha 1")
    String formatted;

    @Schema(description = "Primární název položky (např. ulice+č.p. nebo POI).",
            example = "Týnská ulička 610/7")
    String name;

    @Schema(description = "Typ položky dle provideru (např. address, street, poi).",
            example = "address")
    String label;

    @Schema(description = "např. regional.address, regional.street …",
            example = "street")
    String type;

    @Schema(description = "Lokace položky dle provideru (např. město, čtvť, stát).",
            example = "Praha 1 – Staré Město, Česko")
    String location;

    @Schema(description = "Zeměpisná šířka (WGS-84).", example = "50.087", format = "double")
    Double lat;

    @Schema(description = "Zeměpisná délka (WGS-84).", example = "14.420", format = "double")
    Double lon;

    @Schema(description = "Obálka/bounding box, pokud ji provider posílá.")
    BBox bbox;

    @Schema(description = "Hierarchická struktura regionů (země → kraj → obec → …).")
    List<RegionItem> regionalStructure;

    @Schema(description = "Ulice.", example = "Týnská ulička")
    String street;

    @Schema(description = "Číslo popisné/orientační.", example = "610/7")
    String houseNumber;

    @Schema(description = "Obec.", example = "Praha")
    String municipality;

    @Schema(description = "Městská část / část obce.", example = "Staré Město")
    String municipalityPart;

    @Schema(description = "Kraj/region.", example = "Hlavní město Praha")
    String region;

    @Schema(description = "Stát.", example = "Česká republika")
    String country;

    @Schema(description = "ISO kód státu.", example = "CZ")
    String countryIsoCode;

    @Schema(description = "PSČ.", example = "11000")
    String zip;

    // -------- vnořené typy --------

    @Value
    @Builder
    @Schema(name = "BBox", description = "Bounding box (min/max souřadnice).")
    public static class BBox {
        @Schema(description = "Minimální zeměpisná šířka.", example = "50.086", format = "double")
        Double minLat;
        @Schema(description = "Minimální zeměpisná délka.", example = "14.419", format = "double")
        Double minLon;
        @Schema(description = "Maximální zeměpisná šířka.", example = "50.088", format = "double")
        Double maxLat;
        @Schema(description = "Maximální zeměpisná délka.", example = "14.421", format = "double")
        Double maxLon;
    }

    @Value
    @Builder
    @Schema(name = "RegionItem",
            description = "Položka regionální hierarchie (např. country/region/district/municipality/street).")
    public static class RegionItem {
        @Schema(description = "Typ úrovně (např. country, region, municipality, street).", example = "municipality")
        String type;

        @Schema(description = "Název úrovně.", example = "Praha")
        String name;

        @Schema(description = "Kód úrovně (ISO/NUTS/označení provideru), pokud je k dispozici.", example = "CZ0100")
        String isoCode;
    }
}
