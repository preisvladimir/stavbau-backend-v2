package cz.stavbau.backend.integrations.geo.client;

import cz.stavbau.backend.integrations.geo.config.MapyCzProperties;
import cz.stavbau.backend.integrations.geo.dto.AddressSuggestion;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class MapyCzClient {

    private static final String SUGGEST_PATH = "/v1/geocode";
    private static final String GEOCODE_PATH = "/v1/geocode";
    private final WebClient webClient;
    private final MapyCzProperties props;

    public MapyCzClient(WebClient geoWebClient, MapyCzProperties props) {
        this.webClient = geoWebClient;
        this.props = props;
    }

    public Mono<Map<String, Object>> suggestRaw(String q, Integer limit, String lang) {
        if (!StringUtils.hasText(q)) {
            return Mono.just(Map.of("items", java.util.List.of()));
        }
        //int safeLimit = Math.max(1, Math.min(limit, 10));
        String language = (lang == null || lang.isBlank()) ? "cs" : lang;
        int capped = limit == null ? 7 : Math.max(1, Math.min(10, limit));
        System.out.println("[DEV] query:" + q);
        System.out.println("[DEV] apiKey:" + props.apiKey());
        System.out.println("[DEV] lang:" + language);
        return webClient.get()
                .uri(uri -> uri.path("/v1/geocode")
                        .queryParam("query", q)
                        .queryParam("limit", capped)
                        .queryParam("lang", language)
                        .queryParam("apikey", props.apiKey())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

}