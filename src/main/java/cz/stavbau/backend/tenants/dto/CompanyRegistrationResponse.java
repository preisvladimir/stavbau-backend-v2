// FILE: tenants/dto/CompanyRegistrationResponse.java
package cz.stavbau.backend.tenants.dto;

import java.util.UUID;

public record CompanyRegistrationResponse(
        UUID companyId,
        UUID ownerUserId,
        String ownerRole,
        String status
) {
    public static CompanyRegistrationResponse created(UUID companyId, UUID ownerUserId) {
        return new CompanyRegistrationResponse(companyId, ownerUserId, "OWNER", "CREATED");
    }
}
