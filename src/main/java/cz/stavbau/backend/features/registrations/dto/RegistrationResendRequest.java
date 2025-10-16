package cz.stavbau.backend.features.registrations.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegistrationResendRequest {
    @NotNull(message = "registration.notFound")
    private UUID registrationId;
}
