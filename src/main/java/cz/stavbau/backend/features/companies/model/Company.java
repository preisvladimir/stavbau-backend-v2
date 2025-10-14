package cz.stavbau.backend.features.companies.model;

import cz.stavbau.backend.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies",
        indexes = {
                @Index(name = "ux_company_ico", columnList = "ico", unique = true),
                @Index(name = "ix_company_nuts", columnList = "okres_nuts_lau"),
        })

public class Company extends BaseEntity {
    /** Výchozí jazyk firmy (BCP-47), např. "cs-CZ" nebo "en". */
    @Column(name = "default_locale", length = 8)
    @Pattern(regexp = "^[A-Za-z]{2}(-[A-Za-z]{2})?$", message = "Použijte BCP-47 formát, např. cs-CZ")
    private String defaultLocale;

    @Column(length = 8, nullable = false, unique = true)
    @Pattern(regexp = "\\d{8}", message = "IČO musí mít 8 číslic")
    private String ico;

    /** Obchodní jméno / název subjektu. */
    @Column(length = 255)
    private String obchodniJmeno;

    /** Právní forma (kód ARES/ROS, např. "101"). */
    @Column(name = "pravni_forma_code", length = 8)
    private String pravniFormaCode;

    /** Kód finančního úřadu (např. "057"). */
    @Column(name = "financni_urad_code", length = 8)
    private String financniUradCode;

    /** Datum vzniku subjektu podle ARES. */
    private LocalDate datumVzniku;

    /** Datum poslední aktualizace záznamu v ARES. */
    @Column(name = "datum_aktualizace_ares")
    private LocalDate datumAktualizaceAres;

    /** Kdy jsme naposledy úspěšně synchronizovali z ARES. */
    @Column(name = "ares_last_sync_at")
    private OffsetDateTime aresLastSyncAt;

    /** Převažující CZ-NACE (kód). */
    @Column(name = "cz_nace_prevazujici", length = 6)
    private String czNacePrevazujici;

    /** Geokódy/statistické údaje (lehké převzetí). */
    @Column(name = "zakladni_uzemni_jednotka", length = 16)
    private String zakladniUzemniJednotka;

    @Column(name = "okres_nuts_lau", length = 16)
    private String okresNutsLau;

    @Column(name = "institucionalni_sektor2010", length = 16)
    private String institucionalniSektor2010;

    @Column(name = "kategorie_poctu_pracovniku", length = 8)
    private String kategoriePoctuPracovniku;

    /** Sídlo – strukturovaná adresa (z ARES.sidlo). */
    @Embedded
    private RegisteredAddress sidlo;

    /** Volitelná doručovací adresa (mimo ARES). */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "kodStatu", column = @Column(name = "dor_kod_statu", length = 2)),
            @AttributeOverride(name = "nazevStatu", column = @Column(name = "dor_nazev_statu", length = 64)),
            @AttributeOverride(name = "ulice", column = @Column(name = "dor_ulice", length = 128)),
            @AttributeOverride(name = "cislo", column = @Column(name = "dor_cislo", length = 16)),
            @AttributeOverride(name = "psc", column = @Column(name = "dor_psc", length = 16)),
            @AttributeOverride(name = "obec", column = @Column(name = "dor_obec", length = 128)),
            @AttributeOverride(name = "stat", column = @Column(name = "dor_stat", length = 64))
    })
    private DeliveryAddress adresaDorucovaci;

    /** Stav registrací – ponecháno pro budoucí naplnění (ROS/RŽP/DPH…). */
    @Transient
    private RegistrationStatuses registrace;

    /** Kompletní snapshot ARES odpovědi kvůli auditu/debugu (PostgreSQL JSONB). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ares_raw", columnDefinition = "jsonb")
    private Map<String, Object> aresRaw; // nebo JsonNode / Object

    /** CZ-NACE kódy (více hodnot) – uloženo jako element collection. */
    @ElementCollection
    @CollectionTable(name = "company_nace", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "nace_code", length = 6, nullable = false)
    @Builder.Default
    private Set<String> czNace = new LinkedHashSet<>();

    public @Pattern(regexp = "\\d{8}", message = "IČO musí mít 8 číslic") String getIco() {
        return ico;
    }

    public void setIco(@Pattern(regexp = "\\d{8}", message = "IČO musí mít 8 číslic") String ico) {
        this.ico = ico;
    }

}
