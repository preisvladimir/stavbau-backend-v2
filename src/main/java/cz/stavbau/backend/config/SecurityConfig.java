package cz.stavbau.backend.config;

import cz.stavbau.backend.security.jwt.JwtAuthenticationFilter;
import cz.stavbau.backend.security.rate.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter) {
        this.rateLimitFilter = rateLimitFilter;
    }

    @Value("${app.security.bcrypt-cost:10}")
    private int bcryptCost;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptCost);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwt) throws Exception {
        http
                // 1) Cors
                .cors(Customizer.withDefaults())
                // 2) CSRF – pro stateless REST s JWT vypnout,
                //    případně ignorovat jen konkrétní endpointy:
                .csrf(csrf -> csrf.disable())
                // .csrf(csrf -> csrf.ignoringRequestMatchers(
                //     new AntPathRequestMatcher("/api/v1/tenants/register", "POST"),
                //     new AntPathRequestMatcher("/api/v1/companies/lookup/ares", "GET")
                // ))

                // 3) Public allowlist
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api/v1/auth/**",
                                "/api/v1/ping",
                                "/api/v1/ares/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tenants/register",
                                "/api/v1/companies/register"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/companies/lookup/**",
                                "/api/v1/geo/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 4) Stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 5)  řetězení tvých filtrů (RateLimit před JWT) nech podle tvého nastavení
                // ⬇️ Pořadí filtrů (oběma dáváme kotvu k vestavěnému filtru)
                      // 1) Rate limit před UsernamePasswordAuthenticationFilter
                      .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                      // 2) JWT taktéž před UsernamePasswordAuthenticationFilter (poběží po rate-limit)
                      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
