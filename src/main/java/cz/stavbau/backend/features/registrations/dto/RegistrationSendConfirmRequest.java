package cz.stavbau.backend.features.registrations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class RegistrationSendConfirmRequest {
    @NotNull(message = "registration.notFound")
    private UUID registrationId;

    @Size(max = 128)
    private String idempotencyKey;
}
