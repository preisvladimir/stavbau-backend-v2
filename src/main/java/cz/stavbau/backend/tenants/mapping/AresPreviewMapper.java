package cz.stavbau.backend.tenants.mapping;

import cz.stavbau.backend.tenants.dto.CompanyDto;
import cz.stavbau.backend.tenants.dto.CompanyLookupPreviewDto;
import cz.stavbau.backend.tenants.dto.RegisteredAddressDto;
import org.springframework.stereotype.Component;

@Component
public class AresPreviewMapper {

 public CompanyLookupPreviewDto toPreview(CompanyDto src) {
             var addr = src.getSidlo();
        var previewAddress = new CompanyLookupPreviewDto.AddressDto(
                buildStreet(addr),
                safe(addr != null ? addr.getNazevObce() : null),
                normalizeZip(addr != null ? addr.getPsc() : null),
                upperOrDefault(addr != null ? addr.getKodStatu() : null, "CZ")
        );
        return new CompanyLookupPreviewDto(
                safe(src.getIco()),
                null, // ARES spolehlivě DIČ nedává → FE vyplní/změní ručně
                safe(src.getObchodniJmeno()),
                safe(src.getPravniFormaCode()),
                previewAddress
        );
    }

    private static String buildStreet(RegisteredAddressDto a) {
        if (a == null) return "";
        // Prefer: ulice + číslo domovní; pokud chybí, fallback na textovou adresu
        var streetName = safe(a.getNazevUlice());
        var houseNo = safe(a.getCisloDomovni()); // v aktuálním DTO není číslo orientační
        var joined = (streetName + " " + houseNo).trim();
        if (!joined.isBlank()) return joined.replaceAll("\\s+", " ");
        return safe(a.getTextovaAdresa());
    }

    private static String normalizeZip(String raw) {
        if (raw == null) return "";
        var digits = raw.replaceAll("\\s+", "");
        return digits.replaceFirst("^(\\d{3})(\\d{2})$", "$1 $2"); // 40011 -> 400 11
    }

    private static String upperOrDefault(String s, String def) {
        if (s == null || s.isBlank()) return def;
        return s.toUpperCase();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}