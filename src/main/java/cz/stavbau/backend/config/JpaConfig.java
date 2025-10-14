// src/main/java/cz/stavbau/backend/config/JpaConfig.java
package cz.stavbau.backend.config;

import cz.stavbau.backend.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(modifyOnCreate = true)
public class JpaConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            try {
                return SecurityUtils.currentUserId();
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }
}
