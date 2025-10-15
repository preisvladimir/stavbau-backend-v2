// FILE: tenants/service/RegistrationServiceV1.java
package cz.stavbau.backend.features.registrationV1.service;

import cz.stavbau.backend.features.registrationV1.dto.RegistrationRequestV1;
import cz.stavbau.backend.features.registrationV1.dto.RegistrationResponseV1;

public interface RegistrationServiceV1 {
    RegistrationResponseV1 register(RegistrationRequestV1 request);
}
