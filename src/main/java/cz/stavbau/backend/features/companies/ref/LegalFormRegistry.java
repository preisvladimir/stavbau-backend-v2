// src/main/java/cz/stavbau/backend/tenants/ref/LegalFormRegistry.java
package cz.stavbau.backend.features.companies.ref;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LegalFormRegistry {
    private final Map<String,String> map;

    public LegalFormRegistry() {
        this.map = loadFromCsv("/reference/legal-forms.csv");
    }

    // pro testy
    public LegalFormRegistry(Map<String,String> override) {
        this.map = Map.copyOf(override);
    }

    public Optional<String> resolve(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return Optional.ofNullable(map.get(code.trim()));
    }

    private Map<String,String> loadFromCsv(String path) {
        try (var in = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(path), "Missing " + path),
                StandardCharsets.UTF_8))) {
            return in.lines()
                    .filter(l -> !l.isBlank() && !l.startsWith("#"))
                    .map(l -> l.split(";", 2))
                    .collect(Collectors.toUnmodifiableMap(a -> a[0].trim(), a -> a[1].trim()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
