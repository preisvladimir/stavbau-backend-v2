package cz.stavbau.backend.integrations.geo.service;

import cz.stavbau.backend.integrations.geo.client.MapyCzClient;
import cz.stavbau.backend.integrations.geo.dto.AddressSuggestion;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeoService {

    private final MapyCzClient client;
    private final Cache<String, List<AddressSuggestion>> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(2000)
            .build();

    public GeoService(MapyCzClient client) {
        this.client = client;
    }

    public Mono<List<AddressSuggestion>> suggest(String q, Integer limit, String lang) {
        System.out.println("[service] query:" + q);
        System.out.println("[service] lang:" + lang);
        String nq = normalize(q);
        if (nq.length() < 2) {
            return Mono.just(List.of());
        }
        int capped = limit == null ? 7 : Math.max(1, Math.min(10, limit));
        String cacheKey = "%s|%d|%s".formatted(nq, capped, lang == null ? "" : lang);
        System.out.println("[service] limit:" + capped);
        List<AddressSuggestion> cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }
        System.out.println("[service] volání clienta...");
        return client.suggestRaw(nq, capped, lang)
                .map(this::mapToSuggestions)
                .doOnNext(list -> cache.put(cacheKey, list));
    }

    public Mono<List<AddressSuggestion>> suggestOld(String q, Integer limit, String lang) {
        String nq = normalize(q);
        if (nq.length() < 2) {
            return Mono.just(List.of());
        }
        int capped = limit == null ? 7 : Math.max(1, Math.min(10, limit));
        String cacheKey = "%s|%d|%s".formatted(nq, capped, lang == null ? "" : lang);

        List<AddressSuggestion> cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return client.suggestRaw(nq, capped, lang)
                .map(this::mapToSuggestions)
                .doOnNext(list -> cache.put(cacheKey, list));
    }


    private String normalize(String s) {
        if (!StringUtils.hasText(s)) return "";
        return s.trim();
    }

    @SuppressWarnings("unchecked")
    private List<AddressSuggestion> mapToSuggestions(Object raw) {
        // 1) Získej list položek bez nebezpečných castů
        List<Map<String, Object>> items = List.of();

        if (raw instanceof Map<?, ?> root) {
            Object itemsObj = root.get("items");
            if (itemsObj instanceof List<?> list) {
                items = list.stream()
                        .filter(Map.class::isInstance)
                        .map(e -> (Map<String, Object>) e)
                        .toList();
            }
        } else if (raw instanceof List<?> list) {
            items = list.stream()
                    .filter(Map.class::isInstance)
                    .map(e -> (Map<String, Object>) e)
                    .toList();
        }

        Map<String, AddressSuggestion> uniq = new LinkedHashMap<>();

        for (Map<String, Object> it : items) {
            String name     = asString(it.get("name"));
            String label    = asString(it.get("label"));
            String type     = asString(it.get("type"));
            String location = asString(it.get("location"));
            String zip      = asString(it.get("zip"));

            // position může být Map, ale buďme opatrní
            Map<String, Object> pos = asMap(it.get("position"));
            Double lat = asDouble(pos != null ? pos.get("lat") : null);
            Double lon = asDouble(pos != null ? pos.get("lon") : null);

            // bbox může být Map (minLon/minLat/maxLon/maxLat) – jinak ignoruj
            AddressSuggestion.BBox bbox = null;
            Map<String, Object> bb = asMap(it.get("bbox"));
            if (bb != null) {
                bbox = AddressSuggestion.BBox.builder()
                        .minLon(asDouble(bb.get("minLon")))
                        .minLat(asDouble(bb.get("minLat")))
                        .maxLon(asDouble(bb.get("maxLon")))
                        .maxLat(asDouble(bb.get("maxLat")))
                        .build();
            }

            // regionalStructure je List<Map>, ale ověř typy
            List<AddressSuggestion.RegionItem> regional = List.of();
            List<Object> rs = asList(it.get("regionalStructure"));
            if (rs != null) {
                regional = rs.stream()
                        .filter(Map.class::isInstance)
                        .map(o -> (Map<String, Object>) o)
                        .map(r -> AddressSuggestion.RegionItem.builder()
                                .name(asString(r.get("name")))
                                .type(asString(r.get("type")))
                                .isoCode(asString(r.get("isoCode")))
                                .build())
                        .toList();
            }

            // odvoď běžné části adresy z regionalStructure
            Map<String, String> byType = new HashMap<>();
            for (var r : regional) {
                if (r.getType() != null && r.getName() != null) byType.put(r.getType(), r.getName());
            }
            String street           = byType.get("regional.street");
            String municipality     = byType.get("regional.municipality");
            String municipalityPart = byType.get("regional.municipality_part");
            String region = null;
            var regionsOnly = regional.stream()
                    .filter(r -> "regional.region".equals(r.getType()))
                    .map(AddressSuggestion.RegionItem::getName)
                    .toList();
            if (!regionsOnly.isEmpty()) region = regionsOnly.get(regionsOnly.size() - 1);
            String country    = byType.get("regional.country");
            String countryIso = regional.stream()
                    .filter(r -> "regional.country".equals(r.getType()) && r.getIsoCode() != null)
                    .map(AddressSuggestion.RegionItem::getIsoCode)
                    .findFirst().orElse(null);

            // houseNumber – jednoduchá heuristika z name
            String houseNumber = extractHouseNumber(name);

            // formatted: preferuj "name, location"
            String formatted = (notBlank(name) && notBlank(location)) ? (name + ", " + location)
                    : notBlank(name) ? name
                    : notBlank(location) ? location
                    : "";

            var sug = AddressSuggestion.builder()
                    .formatted(formatted)
                    .name(name).label(label).type(type).location(location).zip(zip)
                    .lat(lat).lon(lon).bbox(bbox)
                    .regionalStructure(regional)
                    .street(street).houseNumber(houseNumber)
                    .municipality(municipality).municipalityPart(municipalityPart)
                    .region(region).country(country).countryIsoCode(countryIso)
                    .build();

            String key = (formatted + "|" + round(lat, 5) + "|" + round(lon, 5));
            uniq.putIfAbsent(key, sug);
        }

        return new ArrayList<>(uniq.values());
    }

    private static Map<String, Object> asMap(Object o) {
        return (o instanceof Map<?, ?> m) ? (Map<String, Object>) m : null;
    }
    private static List<Object> asList(Object o) {
        return (o instanceof List<?> l) ? (List<Object>) l : null;
    }
    private static String asString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }
    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
    private static Double asDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        if (o instanceof String s && !s.isBlank()) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
    private static double round(Double v, int places) {
        if (v == null) return 0d;
        double p = Math.pow(10, places);
        return Math.round(v * p) / p;
    }
    private String extractHouseNumber(String name) {
        if (name == null) return null;
        var m = java.util.regex.Pattern.compile("(\\d+[\\w]*\\/?\\d*[\\w]*)$").matcher(name.trim());
        return m.find() ? m.group(1) : null;
    }


    private String s(Object o) { return o == null ? null : String.valueOf(o); }
    private Double n(Object o) {
        if (o instanceof Number num) return num.doubleValue();
        if (o instanceof String str && !str.isBlank()) try { return Double.parseDouble(str); } catch (NumberFormatException ignored) {}
        return null;
    }

}
