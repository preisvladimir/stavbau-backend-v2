package cz.stavbau.backend.integrations.ares.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "ares")
public class AresProperties {
    /** Např. https://ares.gov.cz/ekonomicke-subjekty-v-be/rest */
    private String baseUrl = "https://ares.gov.cz/ekonomicke-subjekty-v-be/rest";
    /** Timeout v ms */
    private int connectTimeoutMs = 4000;
    private int readTimeoutMs = 6000;
}
