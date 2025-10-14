// FILE: tenants/service/CompanyRegistrationService.java
package cz.stavbau.backend.features.companies.service;

import cz.stavbau.backend.features.companies.dto.CompanyRegistrationRequest;
import cz.stavbau.backend.features.companies.dto.CompanyRegistrationResponse;

public interface CompanyRegistrationService {
    CompanyRegistrationResponse register(CompanyRegistrationRequest request);
}
