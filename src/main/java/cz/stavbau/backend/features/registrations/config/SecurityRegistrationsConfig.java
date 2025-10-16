package cz.stavbau.backend.features.registrations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityRegistrationsConfig {

    public static final String PUBLIC_BASE = "/api/v1/public/registrations/**";

    @Bean
    SecurityFilterChain registrationsPublicChain(HttpSecurity http,
                                                 RegistrationsProperties props) throws Exception {
        http
                .securityMatcher(PUBLIC_BASE)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(HttpMethod.GET, PUBLIC_BASE).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_BASE).permitAll()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(PUBLIC_BASE))
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    CorsConfigurationSource registrationsCorsSource(RegistrationsProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();
        // Z projektových properties můžeme číst FE originy – pokud je nemáš,
        // necháme *explicitně* jen localhost + produkční/staging domény.
        cfg.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://app.stavbau.cz",
                "https://staging.stavbau.cz"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Content-Type","Idempotency-Key","Accept-Language"));
        cfg.setExposedHeaders(List.of("Retry-After","Content-Language"));
        cfg.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(PUBLIC_BASE, cfg);
        return source;
    }
}
