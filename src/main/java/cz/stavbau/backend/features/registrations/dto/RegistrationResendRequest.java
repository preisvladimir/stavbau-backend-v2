package cz.stavbau.backend.features.registrations.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegistrationResendRequest {
    private UUID registrationId;
}
