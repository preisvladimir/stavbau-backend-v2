package cz.stavbau.backend.integrations.geo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "mapycz")
public record MapyCzProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @Positive int timeoutMs
) {}
