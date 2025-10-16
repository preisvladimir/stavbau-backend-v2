package cz.stavbau.backend.features.registrations.service.impl;

import cz.stavbau.backend.features.registrations.service.AresFacade;

import java.util.HashMap;
import java.util.Map;

public class AresFacadeStub implements AresFacade {
    @Override
    public Map<String, Object> lookupByIco(String ico) {
        Map<String, Object> res = new HashMap<>();
        res.put("ico", ico);
        res.put("name", "Demo s.r.o.");
        res.put("active", Boolean.TRUE);
        res.put("address", Map.of("city", "Praha", "country", "CZ"));
        res.put("sourceTs", System.currentTimeMillis());
        return res;
    }
}
