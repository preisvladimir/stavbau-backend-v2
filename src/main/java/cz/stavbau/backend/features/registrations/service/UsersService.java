// src/main/java/cz/stavbau/backend/features/registrations/service/UsersService.java
package cz.stavbau.backend.features.registrations.service;

import java.util.UUID;

public interface UsersService {
    /** Najde existujícího uživatele podle e-mailu; když neexistuje, vytvoří. */
    UUID ensureUserByEmail(String email);
}