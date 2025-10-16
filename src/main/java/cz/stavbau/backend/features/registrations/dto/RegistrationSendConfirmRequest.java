package cz.stavbau.backend.features.registrations.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class RegistrationSendConfirmRequest {
    private UUID registrationId;
    private String idempotencyKey;
}
