// src/main/java/cz/stavbau/backend/features/registrations/service/MembershipService.java
package cz.stavbau.backend.features.registrations.service;

import java.util.UUID;

public interface MembershipService {
    /** Zajistí membership s rolí OWNER; je idempotentní. */
    UUID ensureOwnerMembership(UUID userId, UUID companyId);
}