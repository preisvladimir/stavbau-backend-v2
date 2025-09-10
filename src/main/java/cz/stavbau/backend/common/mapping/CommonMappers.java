package cz.stavbau.backend.common.mapping;

import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Společné MapStruct konverze použitelné napříč celým projektem.
 * Používají se přes @Named v mapperech.
 */
public class CommonMappers {

    // -------------------------
    // UUID <-> String
    // -------------------------

    @Named("uuidToString")
    public String uuidToString(UUID id) {
        return id != null ? id.toString() : null;
    }

    @Named("stringToUuid")
    public UUID stringToUuid(String id) {
        return (id != null && !id.isBlank()) ? UUID.fromString(id) : null;
    }

    // -------------------------
    // Instant <-> OffsetDateTime (UTC)
    // -------------------------

    @Named("instantToOffsetDateTime")
    public OffsetDateTime instantToOffsetDateTime(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }

    @Named("offsetDateTimeToInstant")
    public Instant offsetDateTimeToInstant(OffsetDateTime odt) {
        return odt != null ? odt.toInstant() : null;
    }

    // -------------------------
    // Kolekce Set <-> List
    // -------------------------

    @Named("setToList")
    public <T> List<T> setToList(Set<T> set) {
        return set == null ? Collections.emptyList() : new ArrayList<>(set);
    }

    @Named("listToSet")
    public <T> Set<T> listToSet(List<T> list) {
        return list == null ? Collections.emptySet() : new LinkedHashSet<>(list);
    }
}
