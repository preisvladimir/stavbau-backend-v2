package cz.stavbau.backend.features.members.dto.write;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Vstup pro vytvoření člena")
public record MemberCreateRequest(

        @NotBlank @Email @Schema(example = "user@example.com")
        String email,

        @Size(max = 100) @Schema(example = "Jan")
        String firstName,

        @Size(max = 100) @Schema(example = "Novák")
        String lastName,

        @Size(max = 32) @Schema(example = "+420123456789")
        String phone,

        @NotBlank @Schema(
                description = "Cílová role (CompanyRoleName).",
                example = "MEMBER"
        )
        String role

) {}
