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
    private List<AddressSuggestion> mapToSuggestions(Map<String, Object> raw) {
        Object itemsObj = raw == null ? null : raw.get("items");
        List<Map<String, Object>> items = (itemsObj instanceof List<?> l)
                ? (List<Map<String, Object>>) (List<?>) l
                : List.of();

        return items.stream()
                .map(this::mapItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AddressSuggestion mapItem(Map<String, Object> it) {
        if (it == null) return null;
        String name = str(it.get("name"));
        String label = str(it.get("label"));
        Map<String, Object> position = (Map<String, Object>) it.get("position");
        Double lat = num(position == null ? null : position.get("lat"));
        Double lon = num(position == null ? null : position.get("lon"));

        String location = str(it.get("location"));
        String formatted = StringUtils.hasText(location) ?
                ((StringUtils.hasText(name) ? name + ", " : "") + location) :
                (StringUtils.hasText(name) ? name : label);

        AddressSuggestion.BBox bbox = null;
        Map<String, Object> bboxMap = (Map<String, Object>) it.get("bbox");
        if (bboxMap != null) {
            bbox = AddressSuggestion.BBox.builder()
                    .minLat(num(bboxMap.get("minLat")))
                    .minLon(num(bboxMap.get("minLon")))
                    .maxLat(num(bboxMap.get("maxLat")))
                    .maxLon(num(bboxMap.get("maxLon")))
                    .build();
        }

        List<AddressSuggestion.RegionItem> regions = List.<AddressSuggestion.RegionItem>of();
        Object regObj = it.get("regionalStructure");
        if (regObj instanceof List<?> rl) {
            regions = rl.stream().map(o -> {
                if (o instanceof Map<?,?> m) {
                    return AddressSuggestion.RegionItem.builder()
                            .type(str(m.get("type")))
                            .name(str(m.get("name")))
                            .code(str(m.get("code")))
                            .build();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }

        // Heuristika: extrakce street/houseNumber/municipality/region/zip/country
        String street = null, houseNumber = null, municipality = null, municipalityPart = null, region = null, zip = null, country = null, countryIso = null;
        Map<String, Object> components = (Map<String, Object>) it.get("components");
        if (components != null) {
            street = str(components.get("street"));
            houseNumber = str(components.get("houseNumber"));
            municipality = str(components.get("municipality"));
            municipalityPart = str(components.get("municipalityPart"));
            region = str(components.get("region"));
            zip = str(components.get("zip"));
            country = str(components.get("country"));
            countryIso = str(components.get("countryIsoCode"));
        }

        return AddressSuggestion.builder()
                .formatted(formatted)
                .name(name)
                .label(label)
                .lat(lat)
                .lon(lon)
                .bbox(bbox)
                .regionalStructure(regions)
                .street(street)
                .houseNumber(houseNumber)
                .municipality(municipality)
                .municipalityPart(municipalityPart)
                .region(region)
                .zip(zip)
                .country(country)
                .countryIsoCode(countryIso)
                .build();
    }

    private String str(Object o) { return o == null ? null : String.valueOf(o); }
    private Double num(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
