package cz.stavbau.backend.features.registrations.service;

import java.util.Map;

public interface AresFacade {
    Map<String, Object> lookupByIco(String ico);
}
