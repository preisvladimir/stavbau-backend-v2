package cz.stavbau.backend.features.registrations.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RegistrationStartRequest {
    // CZ IČO (optional for non-CZ)
    private String ico;

    // Company draft (flattened pro PR1; v PR3/5 případně rozpadneme na DTO)
    private Map<String, Object> companyDraft;

    private String email;
    private String password; // Varianta A: v PR4+ neukládáme; necháváme pro FE kontrakt
    private Map<String, Object> consents; // {terms:true, privacy:true, marketingOptIn?:boolean}
    private String captchaToken;
    private String idempotencyKey;
}
