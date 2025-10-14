package cz.stavbau.backend.common.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;
//deprecated
//@Configuration
//@EnableJpaAuditing
public class JpaAuditingConfig {

   // @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            var ctx = org.springframework.security.core.context.SecurityContextHolder.getContext();
            var auth = (ctx != null) ? ctx.getAuthentication() : null;
            if (auth == null || !auth.isAuthenticated()) return java.util.Optional.empty();

            var p = auth.getPrincipal();
            if (p instanceof cz.stavbau.backend.security.AppUserPrincipal u) {
                return java.util.Optional.ofNullable(u.getUserId());
            }
            // anonymous nebo jiný typ → žádný auditor
            return java.util.Optional.empty();
        };
    }
}
