package cz.stavbau.backend.features.registrations.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class RegistrationStartRequest {

    // getters / setters
    // CZ IČO: volitelné, ale pokud vyplněno → 8 číslic
    @Pattern(regexp = "^\\d{8}$", message = "validation.invalidIco")
    private String ico;

    @NotNull(message = "validation.companyDraft.required")
    private Map<String, Object> companyDraft;

    @NotBlank(message = "validation.invalidEmail")
    @Email(message = "validation.invalidEmail")
    @Size(max = 254, message = "validation.invalidEmail")
    private String email;

    // Varianta A: heslo pro teď nevyžadujeme (set-password později)
    @Size(min = 0, max = 256, message = "validation.passwordPolicyFailed")
    private String password;

    @NotNull(message = "validation.consents.required")
    private Map<String, Object> consents; // FE musí poslat {terms:true, privacy:true}

    @NotBlank(message = "captcha.failed")
    @Size(max = 4096, message = "captcha.failed")
    private String captchaToken;

    @Size(max = 128)
    private String idempotencyKey;

}
