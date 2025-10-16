package cz.stavbau.backend.features.registrations.api;

import cz.stavbau.backend.features.registrations.dto.*;
import cz.stavbau.backend.features.registrations.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/public/registrations", produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    @PostMapping(path = "/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationResponse start(@Valid @RequestBody RegistrationStartRequest body,
                                      @RequestHeader(name = "Idempotency-Key", required = false) String idemKey,
                                      @RequestHeader(name = "Accept-Language", required = false) Locale locale,
                                      HttpServletRequest req) {
        // Pozn.: prozatím jen přepošleme DTO; IP/UA zatím necpeme do služby (viz PR3 komentáře).
        if (idemKey != null && (body.getIdempotencyKey() == null || body.getIdempotencyKey().isBlank())) {
            body.setIdempotencyKey(idemKey);
        }
        return service.start(body);
    }

    @PostMapping(path = "/send-confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationResponse sendConfirm(@Valid @RequestBody RegistrationSendConfirmRequest body,
                                            @RequestHeader(name = "Idempotency-Key", required = false) String idemKey) {
        if (idemKey != null && (body.getIdempotencyKey() == null || body.getIdempotencyKey().isBlank())) {
            body.setIdempotencyKey(idemKey);
        }
        return service.sendConfirm(body);
    }

    @PostMapping(path = "/resend", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationResponse resend(@Valid @RequestBody RegistrationResendRequest body) {
        return service.resend(body);
    }

    @GetMapping(path = "/confirm")
    public RegistrationResponse confirm(@RequestParam("token") String token) {
        return service.confirm(token);
    }

    @GetMapping(path = "/status/{registrationId}")
    public RegistrationStatusResponse status(@PathVariable("registrationId") UUID id) {
        return service.status(id);
    }
}
