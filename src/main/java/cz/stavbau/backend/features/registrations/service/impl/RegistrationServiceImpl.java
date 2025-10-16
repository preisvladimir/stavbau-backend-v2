// src/main/java/cz/stavbau/backend/features/registrations/service/impl/RegistrationServiceImpl.java
package cz.stavbau.backend.features.registrations.service.impl;

import cz.stavbau.backend.features.registrations.config.RegistrationsProperties;
import cz.stavbau.backend.features.registrations.dto.*;
import cz.stavbau.backend.features.registrations.model.RegistrationCase;
import cz.stavbau.backend.features.registrations.repo.RegistrationCaseRepository;
import cz.stavbau.backend.features.registrations.service.*;
import cz.stavbau.backend.features.registrations.service.exceptions.RegistrationExceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RegistrationServiceImpl implements RegistrationService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("EMAIL_SENT","EMAIL_VERIFIED","APPROVED");
    private static final Set<String> TERMINAL_STATUSES = Set.of("EXPIRED","CANCELLED","FAILED");

    private final RegistrationCaseRepository repo;
    private final TokenService tokenService;
    private final RateLimitService rateLimit;
    private final CaptchaService captcha;
    private final Mailer mailer;
    private final AresFacade ares;
    private final UsersService users;
    private final CompaniesService companies;
    private final MembershipService memberships;
    private final RegistrationsProperties props;
    private final Clock clock;
    private final ObjectMapper om;

    public RegistrationServiceImpl(RegistrationCaseRepository repo,
                                   TokenService tokenService,
                                   RateLimitService rateLimit,
                                   CaptchaService captcha,
                                   Mailer mailer,
                                   AresFacade ares,
                                   UsersService users,
                                   CompaniesService companies,
                                   MembershipService memberships,
                                   RegistrationsProperties props,
                                   Clock clock,
                                   ObjectMapper objectMapper) {
        this.repo = repo;
        this.tokenService = tokenService;
        this.rateLimit = rateLimit;
        this.captcha = captcha;
        this.mailer = mailer;
        this.ares = ares;
        this.users = users;
        this.companies = companies;
        this.memberships = memberships;
        this.props = props;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.om = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    // ===== /start =====
    @Override
    @Transactional
    public RegistrationResponse start(RegistrationStartRequest request) {
        // 1) anti-bot / rate-limit (IP z infra – zde dummy null; v controlleru doplníme)
        InetAddress clientIp = null;
        captcha.verify(request.getCaptchaToken(), clientIp);
        rateLimit.checkStartAllowance(request.getEmail(), clientIp);

        // 2) pokud existuje aktivní case pro e-mail → idempotentní návrat
        List<RegistrationCase> existing = repo.findActiveByEmail(request.getEmail());
        if (!existing.isEmpty()) {
            RegistrationCase rc = existing.get(0); // pokud víc, bereme nejnovější by design; slim
            return toResponse(rc, List.of(msg("registration.email.sent","INFO", Map.of())));
        }

        // 3) ARES lookup (pokud ICO)
        Map<String, Object> aresSnapshot = null;
        if (request.getIco() != null && !request.getIco().isBlank()) {
            aresSnapshot = ares.lookupByIco(request.getIco());
        }

        // 4) vytvořit nový case
        Instant now = Instant.now(clock);
        Duration caseTtl = Duration.parse(props.getACase().getTtl());
        Duration resendCooldown = Duration.parse(props.getResend().getCooldown());

        TokenService.GeneratedToken token = tokenService.issueVerificationToken();

        RegistrationCase rc = new RegistrationCase();
        rc.setId(UUID.randomUUID());
        rc.setStatus("EMAIL_SENT");
        rc.setNextAction("VERIFY_EMAIL");
        rc.setEmail(request.getEmail());
        rc.setTokenHash(token.tokenHash);
        rc.setTokenExpiresAt(token.expiresAt);
        rc.setCooldownUntil(now.plus(resendCooldown));
        rc.setIdempotencyKey(request.getIdempotencyKey());
        rc.setCompanyDraft(jsonOrEmpty(mergeCompanyDraft(request.getCompanyDraft(), request.getIco(), aresSnapshot)));
        rc.setConsents(jsonOrEmpty(augmentConsents(request.getConsents(), now, null)));
        rc.setRequestedIp(null); // doplníme v controlleru
        rc.setUserAgent(null);   // doplníme v controlleru
        rc.setLocale(extractLocale(request));
        rc.setAresLookupMeta(aresSnapshot == null ? null : jsonOrEmpty(aresSnapshot));
        rc.setCompanyId(null);
        rc.setOwnerUserId(null);
        rc.setExpiresAt(now.plus(caseTtl));
        rc.setCreatedAt(now);
        rc.setUpdatedAt(now);
        rc.setCreatedBy("system");
        rc.setUpdatedBy("system");
        rc.setDeletedAt(null);
        rc.setVersion(0);

        repo.save(rc);

        // 5) e-mail
        mailer.sendVerificationEmail(
                rc.getEmail(),
                localeToJava(rc.getLocale()),
                emailModel(rc, token.rawToken)
        );

        rateLimit.onStartCommitted(request.getEmail(), clientIp);
        return toResponse(rc, List.of(msg("registration.email.sent","INFO", Map.of())));
    }

    // ===== /send-confirm =====
    @Override
    @Transactional
    public RegistrationResponse sendConfirm(RegistrationSendConfirmRequest request) {
        InetAddress clientIp = null;
        // treat like resend for per-email limit
        rateLimit.checkResendAllowance("", clientIp); // email doplníme až po načtení rc

        RegistrationCase rc = repo.findByIdForUpdate(request.getRegistrationId())
                .orElseThrow(() -> new NotFound("registration.notFound"));

        // opravdový rate-limit na email až teď
        rateLimit.checkResendAllowance(rc.getEmail(), clientIp);

        if (isTerminal(rc.getStatus()) || "COMPANY_CREATED".equals(rc.getStatus()) || "EMAIL_VERIFIED".equals(rc.getStatus())) {
            return toResponse(rc, List.of(msg("registration.alreadyConfirmed","INFO", Map.of())));
        }

        Instant now = Instant.now(clock);
        Duration resendCooldown = Duration.parse(props.getResend().getCooldown());

        if (rc.getCooldownUntil() != null && rc.getCooldownUntil().isAfter(now)) {
            // cooldown – neobnovujeme token, jen vrátíme stav
            return toResponse(rc, List.of(msg("registration.resend.cooldownActive","INFO",
                    Map.of("cooldownUntil", rc.getCooldownUntil()))));
        }

        // token ještě platí? Stačí poslat znovu a nastavit cooldown
        if (rc.getTokenExpiresAt() != null && rc.getTokenExpiresAt().isAfter(now)) {
            rc.setCooldownUntil(now.plus(resendCooldown));
            rc.setUpdatedAt(now);
            repo.save(rc);
            mailer.sendVerificationEmail(rc.getEmail(), localeToJava(rc.getLocale()), emailModel(rc, null)); // pokud máme raw token?
            // Raw token nemáme – v tomto režimu by link potřeboval raw. V praxi FE nepotřebuje nový – už ho má z původního mailu.
            // V PR4 přepneme na vždy-regen token v /resend a ponecháme zde "confirm that was sent".
            rateLimit.onResendCommitted(rc.getEmail(), clientIp);
            return toResponse(rc, List.of(msg("registration.email.sent","INFO", Map.of())));
        }

        // token expirován → vygenerovat nový
        TokenService.GeneratedToken token = tokenService.issueVerificationToken();
        rc.setTokenHash(token.tokenHash);
        rc.setTokenExpiresAt(token.expiresAt);
        rc.setCooldownUntil(now.plus(resendCooldown));
        rc.setUpdatedAt(now);
        repo.save(rc);

        mailer.sendVerificationEmail(rc.getEmail(), localeToJava(rc.getLocale()), emailModel(rc, token.rawToken));
        rateLimit.onResendCommitted(rc.getEmail(), clientIp);
        return toResponse(rc, List.of(msg("registration.email.sent","INFO", Map.of())));
    }

    // ===== /resend =====
    @Override
    @Transactional
    public RegistrationResponse resend(RegistrationResendRequest request) {
        InetAddress clientIp = null;

        RegistrationCase rc = repo.findByIdForUpdate(request.getRegistrationId())
                .orElseThrow(() -> new NotFound("registration.notFound"));

        rateLimit.checkResendAllowance(rc.getEmail(), clientIp);

        if (isTerminal(rc.getStatus()) || "COMPANY_CREATED".equals(rc.getStatus()) || "EMAIL_VERIFIED".equals(rc.getStatus())) {
            return toResponse(rc, List.of(msg("registration.alreadyConfirmed","INFO", Map.of())));
        }

        Instant now = Instant.now(clock);
        Duration resendCooldown = Duration.parse(props.getResend().getCooldown());

        if (rc.getCooldownUntil() != null && rc.getCooldownUntil().isAfter(now)) {
            return toResponse(rc, List.of(msg("registration.resend.cooldownActive","INFO",
                    Map.of("cooldownUntil", rc.getCooldownUntil()))));
        }

        // Vždy vygenerujeme nový token (resend = forced new)
        TokenService.GeneratedToken token = tokenService.issueVerificationToken();
        rc.setTokenHash(token.tokenHash);
        rc.setTokenExpiresAt(token.expiresAt);
        rc.setCooldownUntil(now.plus(resendCooldown));
        rc.setUpdatedAt(now);
        repo.save(rc);

        mailer.sendVerificationEmail(rc.getEmail(), localeToJava(rc.getLocale()), emailModel(rc, token.rawToken));
        rateLimit.onResendCommitted(rc.getEmail(), clientIp);
        return toResponse(rc, List.of(msg("registration.email.sent","INFO", Map.of())));
    }

    // ===== /confirm =====
    @Override
    @Transactional
    public RegistrationResponse confirm(String rawToken) {
        String tokenHash = tokenService.hash(rawToken);
        RegistrationCase rc = repo.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(TokenInvalidOrExpired::new);

        Instant now = Instant.now(clock);
        if (rc.getTokenExpiresAt() == null || !rc.getTokenExpiresAt().isAfter(now)) {
            throw new TokenInvalidOrExpired();
        }

        // Idempotence: pokud už proběhl confirm
        if ("COMPANY_CREATED".equals(rc.getStatus()) || "EMAIL_VERIFIED".equals(rc.getStatus())) {
            return toResponse(rc, List.of(msg("registration.alreadyConfirmed","INFO", Map.of())));
        }

        // Consume token
        rc.setTokenHash(null);
        rc.setTokenExpiresAt(null);
        rc.setStatus("EMAIL_VERIFIED");
        rc.setNextAction("NONE");
        rc.setUpdatedAt(now);

        // Atomicky vytvořit company + owner
        UUID userId = users.ensureUserByEmail(rc.getEmail());
        Map<String, Object> draft = parseJson(rc.getCompanyDraft());
        UUID companyId = companies.createCompanyFromDraft(draft);
        memberships.ensureOwnerMembership(userId, companyId);

        rc.setOwnerUserId(userId);
        rc.setCompanyId(companyId);
        rc.setStatus("COMPANY_CREATED");
        rc.setUpdatedAt(now);
        repo.save(rc);

        return toResponse(rc, List.of(msg("company.created","INFO", Map.of("companyId", companyId.toString()))));
    }

    // ===== /status =====
    @Override
    @Transactional(readOnly = true)
    public RegistrationStatusResponse status(UUID registrationId) {
        RegistrationCase rc = repo.findById(registrationId)
                .orElseThrow(() -> new NotFound("registration.notFound"));
        RegistrationStatusResponse resp = new RegistrationStatusResponse();
        resp.setRegistrationId(rc.getId());
        resp.setStatus(rc.getStatus());
        resp.setNextAction(rc.getNextAction());
        resp.setExpiresAt(rc.getExpiresAt());
        resp.setCooldownUntil(rc.getCooldownUntil());
        return resp;
    }

    // ===== Helpers =====

    private boolean isTerminal(String status) {
        return TERMINAL_STATUSES.contains(status);
    }

    private String extractLocale(RegistrationStartRequest req) {
        // fallback na první allowed locale
        String fromDraft = null;
        if (req.getCompanyDraft() != null) {
            Object l = req.getCompanyDraft().get("locale");
            if (l instanceof String s && !s.isBlank()) fromDraft = s;
        }
        List<String> allowed = props.getLocales().getAllowed();
        String candidate = fromDraft != null ? fromDraft : (allowed.isEmpty() ? "cs-CZ" : allowed.get(0));
        return allowed.contains(candidate) ? candidate : allowed.get(0);
    }

    private Map<String, Object> mergeCompanyDraft(Map<String, Object> draft, String ico, Map<String, Object> ares) {
        Map<String, Object> res = draft == null ? new LinkedHashMap<>() : new LinkedHashMap<>(draft);
        if (ico != null && !ico.isBlank()) res.put("ico", ico);
        if (ares != null) {
            res.put("ares", ares);
            if (!res.containsKey("name") && ares.get("name") != null) res.put("name", ares.get("name"));
            if (!res.containsKey("address") && ares.get("address") != null) res.put("address", ares.get("address"));
        }
        return res;
    }

    private Map<String, Object> augmentConsents(Map<String, Object> consents, Instant now, String ip) {
        Map<String, Object> c = consents == null ? new LinkedHashMap<>() : new LinkedHashMap<>(consents);
        c.putIfAbsent("terms", Boolean.TRUE);
        c.put("termsAt", now.toString());
        c.putIfAbsent("privacy", Boolean.TRUE);
        c.put("privacyAt", now.toString());
        if (ip != null) {
            c.put("termsIp", ip);
            c.put("privacyIp", ip);
        }
        return c;
    }

    private String jsonOrEmpty(Object o) {
        try {
            return om.writeValueAsString(o == null ? Map.of() : o);
        } catch (JsonProcessingException e) {
            throw new Validation("internal.json.serialize");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try {
            return om.readValue(json, LinkedHashMap.class);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> emailModel(RegistrationCase rc, String rawTokenOrNull) {
        Map<String, Object> model = new HashMap<>();
        String base = props.getPublicBaseUrl();
        if (rawTokenOrNull != null) {
            model.put("verificationLink", base + "/registrations/confirm?token=" + rawTokenOrNull);
        } else {
            // fallback text – FE používá původní e-mail; zde jen redundance
            model.put("verificationLink", base + "/registrations/info");
        }
        model.put("expiresAt", rc.getTokenExpiresAt());
        model.put("appName", "STAVBAU");
        return model;
    }

    private RegistrationResponse.Message msg(String code, String level, Map<String, Object> params) {
        RegistrationResponse.Message m = new RegistrationResponse.Message();
        m.setCode(code);
        m.setLevel(level);
        m.setParams(params);
        return m;
    }

    private RegistrationResponse toResponse(RegistrationCase rc, List<RegistrationResponse.Message> msgs) {
        RegistrationResponse resp = new RegistrationResponse();
        resp.setRegistrationId(rc.getId());
        resp.setStatus(rc.getStatus());
        resp.setNextAction(rc.getNextAction());
        resp.setExpiresAt(rc.getExpiresAt());
        resp.setCooldownUntil(rc.getCooldownUntil());
        resp.setMessages(msgs);
        return resp;
    }

    private Locale localeToJava(String l) {
        if (l == null || l.isBlank()) return Locale.getDefault();
        String[] parts = l.split("[-_]");
        if (parts.length == 1) return new Locale(parts[0]);
        return new Locale(parts[0], parts[1]);
    }
}
