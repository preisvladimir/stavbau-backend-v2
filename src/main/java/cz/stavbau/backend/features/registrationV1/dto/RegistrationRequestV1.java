package cz.stavbau.backend.features.registrationV1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegistrationRequestV1(
        @Valid @NotNull CompanyDto company,
        @Valid @NotNull OwnerDto owner,
        @Valid @NotNull ConsentsDto consents
) {
    public record CompanyDto(
            @NotBlank @Pattern(regexp = "\\d{8}", message = "{validation.ico.invalid}") String ico,
            String dic,
            @NotBlank String name,
            @Valid @NotNull AddressDto address,
            String legalFormCode
    ) {}
    public record AddressDto(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank @Pattern(regexp = "\\d{3}\\s?\\d{2}") String zip,
            @NotBlank String country
    ) {}
    public record OwnerDto(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            String firstName,         // MVP: neperzistujeme
            String lastName,          // MVP: neperzistujeme
            String phone              // MVP: neperzistujeme
    ) {}
    public record ConsentsDto(
            @AssertTrue(message = "{validation.terms.required}") boolean termsAccepted,
            Boolean marketing
    ) {}
}
