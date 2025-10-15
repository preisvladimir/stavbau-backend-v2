package cz.stavbau.backend.integrations.ares.mapper;

import cz.stavbau.backend.features.companies.dto.CompanyDto;
import cz.stavbau.backend.integrations.ares.dto.AresPreviewDto;
import cz.stavbau.backend.features.companies.dto.RegisteredAddressDto;
import cz.stavbau.backend.features.companies.ref.LegalFormRegistry;
import org.springframework.stereotype.Component;

@Component
public class AresPreviewMapper {
   private final LegalFormRegistry legalForms;
   public AresPreviewMapper(LegalFormRegistry legalForms) { this.legalForms = legalForms; }

 public AresPreviewDto toPreview(CompanyDto src) {
        var addr = src.getSidlo();
        var legalFormCode = src.getPravniFormaCode();
        var legalFormName = legalForms.resolve(legalFormCode).orElse(null);
        var previewAddress = new AresPreviewDto.AddressDto(
                buildStreet(addr),
                safe(addr != null ? addr.getNazevObce() : null),
                normalizeZip(addr != null ? addr.getPsc() : null),
                upperOrDefault(addr != null ? addr.getKodStatu() : null, "CZ")
        );
        return new AresPreviewDto(
                safe(src.getIco()),
                null, // ARES spolehlivě DIČ nedává → FE vyplní/změní ručně
                safe(src.getObchodniJmeno()),
                safe(legalFormCode),
                safe(legalFormName),
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