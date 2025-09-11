package cz.stavbau.backend.integrations.geo.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(MapyCzProperties.class)
public class GeoWebClientConfig {

    @Bean
    public WebClient geoWebClient(MapyCzProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.timeoutMs())
                .responseTimeout(Duration.ofMillis(props.timeoutMs()))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(props.timeoutMs(), TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(props.timeoutMs(), TimeUnit.MILLISECONDS));
                });

        ExchangeFilterFunction errorFilter = ExchangeFilterFunction.ofResponseProcessor(resp -> {
            if (resp.statusCode().isError()) {
                return resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.error(new GeoProviderException(
                                "Mapy.cz error %s: %s".formatted(resp.statusCode(), body))));
            }
            return Mono.just(resp);
        });

        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("User-Agent", "Stavbau-Backend/geo (MVP)")
                .filter(errorFilter)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
