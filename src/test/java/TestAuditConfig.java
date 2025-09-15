package cz.stavbau.backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@TestConfiguration
@EnableJpaAuditing
public class TestAuditConfig {

    @Bean
    AuditorAware<UUID> auditorAware() {
        return Optional::empty; // anonymous = žádný auditor
    }
}
