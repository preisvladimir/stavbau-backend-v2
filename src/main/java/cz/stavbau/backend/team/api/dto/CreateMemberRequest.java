package cz.stavbau.backend.team.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vstup pro vytvoření člena")
public record CreateMemberRequest(
        @NotBlank @Email @Schema(example = "user@example.com")
        String email,
        @NotBlank @Schema(allowableValues = {"ADMIN","MEMBER"}, example = "MEMBER")
        String role,
        @Schema(example = "Jan") String firstName,
        @Schema(example = "Novák") String lastName,
        @Schema(example = "+420777123456") String phone
) {}
