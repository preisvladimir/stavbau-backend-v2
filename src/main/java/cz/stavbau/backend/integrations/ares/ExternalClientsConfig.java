package cz.stavbau.backend.integrations.ares;

import cz.stavbau.backend.integrations.ares.config.AresProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({AresProperties.class})
public class ExternalClientsConfig {

    @Bean
    public WebClient aresWebClient(AresProperties props) {
        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(props.getReadTimeoutMs() / 1000))
                        .addHandlerLast(new WriteTimeoutHandler(props.getReadTimeoutMs() / 1000)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .baseUrl(props.getBaseUrl())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2 MB
                        .build())
                .build();
    }
}
