// FILE: src/main/java/cz/stavbau/backend/integrations/weather/MeteostatRapidApiConfig.java
package cz.stavbau.backend.integrations.weather;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class MeteostatRapidApiConfig {

    @Bean("meteostatWebClient")
    public WebClient meteostatWebClient(
            @Value("${meteostat.base-url}") String baseUrl,
            @Value("${meteostat.rapidapi.host}") String rapidHost,
            @Value("${meteostat.rapidapi.key}") String rapidKey
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6_000)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(8, TimeUnit.SECONDS));
                });

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("x-rapidapi-host", rapidHost)
                .defaultHeader("x-rapidapi-key", rapidKey)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
                        .build())
                .build();
    }
}
