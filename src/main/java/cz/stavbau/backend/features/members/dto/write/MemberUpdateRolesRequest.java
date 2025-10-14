package cz.stavbau.backend.features.members.dto.write;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Změna role člena")
public record MemberUpdateRolesRequest(
        @NotBlank
        @Schema(
                description = "Cílová role (CompanyRoleName). OWNER/SUPERADMIN nejsou touto cestou povoleny.",
                example = "MEMBER"
        )
        String role
) {}
