package cz.stavbau.backend.features.registrations.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class RegistrationStatusResponse {
    private UUID registrationId;
    private String status;     // RegistrationStatus
    private String nextAction; // NextAction
    private Instant expiresAt;
    private Instant cooldownUntil;
}
