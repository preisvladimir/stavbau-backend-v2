package cz.stavbau.backend.features.registrations.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.stavbau.backend.features.registrations.mailer.MockMailer;
import cz.stavbau.backend.features.registrations.mailer.SmtpMailer;
import cz.stavbau.backend.features.registrations.repo.RegistrationCaseRepository;
import cz.stavbau.backend.features.registrations.service.*;
import cz.stavbau.backend.features.registrations.service.impl.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(RegistrationsProperties.class)
public class RegistrationsConfig {

    @Bean
    public Clock registrationsClock() {
        return Clock.systemUTC();
    }

    @Bean
    public TokenService tokenService(RegistrationsProperties props, Clock clock) {
        return new TokenServiceImpl(props, clock);
    }

    @Bean
    public CaptchaService captchaService(RegistrationsProperties props) {
        // V PR2 jen mock; Recaptcha provider přidáme později.
        return new CaptchaServiceMock();
    }

    @Bean
    public RateLimitService rateLimitService(RegistrationsProperties props,
                                             JdbcTemplate jdbc,
                                             StringRedisTemplate redisTemplate,
                                             Clock clock) {
        String backend = props.getRatelimit().getBackend();
        if ("redis".equalsIgnoreCase(backend)) {
            return new RateLimitServiceRedis(redisTemplate, props, clock);
        }
        return new RateLimitServicePostgres(jdbc, props, clock);
    }
    @Bean
    public AresFacade aresFacade(RegistrationsProperties props) {
        // PR2: stub; skutečnou integraci navážeme v dalším PR, nebo v projekčním modulu mimo registrations.
        return new AresFacadeStub();
    }

    @Bean
    public RegistrationService registrationService(RegistrationCaseRepository repo,
                                                   TokenService tokenService,
                                                   RateLimitService rateLimitService,
                                                   CaptchaService captchaService,
                                                   Mailer mailer,
                                                   AresFacade aresFacade,
                                                   UsersService usersService,
                                                   CompaniesService companiesService,
                                                   MembershipService membershipService,
                                                   RegistrationsProperties props,
                                                   Clock clock,
                                                   ObjectMapper objectMapper) {
        return new RegistrationServiceImpl(
                repo, tokenService, rateLimitService, captchaService, mailer, aresFacade,
                usersService, companiesService, membershipService, props, clock, objectMapper
        );
    }

    @Bean
    public Mailer mailer(RegistrationsProperties props,
                         JavaMailSender javaMailSender,
                         TemplateEngine registrationMailTemplateEngine,
                         MessageSource messageSource) {

        String provider = props.getMail().getProvider();
        if ("mock".equalsIgnoreCase(provider)) {
            return new MockMailer();
        }
        // default i "smtp"
        return new SmtpMailer(javaMailSender, registrationMailTemplateEngine, messageSource, props);
    }



}
