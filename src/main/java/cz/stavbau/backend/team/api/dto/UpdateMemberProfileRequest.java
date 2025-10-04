package cz.stavbau.backend.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Úprava profilových údajů člena")
public record UpdateMemberProfileRequest(
        @Email @Schema(example = "user@example.com")
        String email,

        @Size(max = 100) @Schema(example = "Jan")
        String firstName,

        @Size(max = 100) @Schema(example = "Novák")
        String lastName,

        @Size(max = 32) @Schema(example = "+420123456789")
        String phone

) {}