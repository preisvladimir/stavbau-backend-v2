// src/main/java/cz/stavbau/backend/features/registrations/service/CompaniesService.java
package cz.stavbau.backend.features.registrations.service;

import java.util.Map;
import java.util.UUID;

public interface CompaniesService {
    /** Vytvoří Company z draftu; vyhodí výjimku při kolizi podle interních pravidel. */
    UUID createCompanyFromDraft(Map<String, Object> companyDraft);
}
