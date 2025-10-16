package cz.stavbau.backend.features.registrations.dev;

import cz.stavbau.backend.features.registrations.service.CompaniesService;
import cz.stavbau.backend.features.registrations.service.MembershipService;
import cz.stavbau.backend.features.registrations.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.UUID;

@Configuration
@Profile("dev") // aktivuj tím, že spouštíš profil dev
public class RegistrationsDevStubsConfig {
    private static final Logger log = LoggerFactory.getLogger(RegistrationsDevStubsConfig.class);

    @Bean
    @Primary
    public UsersService usersServiceStub() {
        return email -> {
            UUID id = UUID.randomUUID();
            log.info("[STUB] ensureUserByEmail email={} -> userId={}", email, id);
            return id;
        };
    }

    @Bean
    @Primary
    public CompaniesService companiesServiceStub() {
        return (Map<String, Object> draft) -> {
            UUID id = UUID.randomUUID();
            log.info("[STUB] createCompanyFromDraft draftKeys={} -> companyId={}",
                    draft == null ? 0 : draft.keySet(), id);
            return id;
        };
    }

    @Bean
    @Primary
    public MembershipService membershipServiceStub() {
        return (userId, companyId) -> {
            UUID id = UUID.randomUUID();
            log.info("[STUB] ensureOwnerMembership userId={} companyId={} -> membershipId={}",
                    userId, companyId, id);
            return id;
        };
    }
}
