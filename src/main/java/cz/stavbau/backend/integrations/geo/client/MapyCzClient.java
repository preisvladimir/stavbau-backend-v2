package cz.stavbau.backend.integrations.geo.client;

import cz.stavbau.backend.integrations.geo.config.MapyCzProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
public class MapyCzClient {

    private static final String SUGGEST_PATH = "/v1/geocode";
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
        int capped = limit == null ? 7 : Math.max(1, Math.min(10, limit));
        return webClient.get()
                .uri(builder -> builder
                        .path(SUGGEST_PATH)
                        .queryParam("q", q)
                        .queryParam("limit", capped)
                        .queryParamIfPresent("lang", Optional.ofNullable(lang))
                        .queryParam("apikey", props.apiKey())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
