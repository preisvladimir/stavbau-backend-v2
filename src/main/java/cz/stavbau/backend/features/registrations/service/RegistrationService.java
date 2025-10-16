package cz.stavbau.backend.features.registrations.service;

import cz.stavbau.backend.features.registrations.dto.*;

import java.util.UUID;

public interface RegistrationService {
    RegistrationResponse start(RegistrationStartRequest request);
    RegistrationResponse sendConfirm(RegistrationSendConfirmRequest request);
    RegistrationResponse resend(RegistrationResendRequest request);
    RegistrationResponse confirm(String rawToken);
    RegistrationStatusResponse status(UUID registrationId);
}
