package cz.stavbau.backend.security.rbac;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/** Zapnutí method security pro @PreAuthorize (RBAC 2.1 §7). */
@Configuration
@EnableMethodSecurity
public class RbacMethodSecurityConfig {
    // Není potřeba nic dalšího – SpEL bean "rbac" poskytuje RbacSpelEvaluator.
}
