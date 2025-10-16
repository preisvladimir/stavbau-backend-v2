package cz.stavbau.backend.features.registrations.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RegistrationResponse {

    @Setter
    @Getter
    public static class Message {
        private String code;
        private String level; // INFO|WARNING|ERROR
        private Map<String, Object> params;

    }

    private UUID registrationId;
    private String status;      // RegistrationStatus (STRING)
    private String nextAction;  // NextAction (STRING)
    private Instant expiresAt;
    private Instant cooldownUntil;
    private List<Message> messages;

}
