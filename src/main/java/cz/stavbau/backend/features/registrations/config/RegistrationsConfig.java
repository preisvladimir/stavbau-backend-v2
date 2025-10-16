package cz.stavbau.backend.features.registrations.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RegistrationsProperties.class)
public class RegistrationsConfig {
    // PR1: bez @Bean wiring; poskytuje pouze načtení properties
}
