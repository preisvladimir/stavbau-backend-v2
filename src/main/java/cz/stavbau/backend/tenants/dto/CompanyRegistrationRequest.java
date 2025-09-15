// FILE: tenants/dto/CompanyRegistrationRequest.java
package cz.stavbau.backend.tenants.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Objects;

public record CompanyRegistrationRequest(
        @Valid @NotNull CompanyDto company,
        @Valid @NotNull OwnerDto owner,
        @Valid @NotNull ConsentsDto consents
) {
    public record CompanyDto(
            @NotBlank @Pattern(regexp = "\\d{8}", message = "{validation.ico.invalid}") String ico,
            @Pattern(regexp = "^[A-Z]{2}\\d[\\dA-Z]*$", message = "{validation.dic.invalid}") String dic,
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
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phone
    ) {}
    public record ConsentsDto(
            @AssertTrue(message = "{validation.terms.required}") boolean termsAccepted,
            Boolean marketing
    ) {}

    public CompanyRegistrationRequest {
        Objects.requireNonNull(company);
        Objects.requireNonNull(owner);
        Objects.requireNonNull(consents);
    }
}
