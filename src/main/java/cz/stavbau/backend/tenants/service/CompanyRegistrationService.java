// FILE: tenants/service/CompanyRegistrationService.java
package cz.stavbau.backend.tenants.service;

import cz.stavbau.backend.tenants.dto.CompanyRegistrationRequest;
import cz.stavbau.backend.tenants.dto.CompanyRegistrationResponse;

public interface CompanyRegistrationService {
    CompanyRegistrationResponse register(CompanyRegistrationRequest request);
}
