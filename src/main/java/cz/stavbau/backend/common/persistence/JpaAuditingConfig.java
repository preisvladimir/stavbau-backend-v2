package cz.stavbau.backend.common.persistence;

import org.springframework.context.annotation.*; import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.*; import cz.stavbau.backend.security.SecurityUtils;

@Configuration @EnableJpaAuditing
public class JpaAuditingConfig {
    @Bean AuditorAware<UUID> auditorAware() {
        return () -> Optional.ofNullable(SecurityUtils.currentUserId());
    }
}
