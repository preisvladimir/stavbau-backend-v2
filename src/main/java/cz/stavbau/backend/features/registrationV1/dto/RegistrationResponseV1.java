package cz.stavbau.backend.features.registrationV1.dto;

import java.util.UUID;

public record RegistrationResponseV1(
        UUID companyId,
        UUID ownerUserId,
        String ownerRole,
        String status
) {
    public static RegistrationResponseV1 created(UUID companyId, UUID ownerUserId) {
        return new RegistrationResponseV1(companyId, ownerUserId, "OWNER", "CREATED");
    }
}
