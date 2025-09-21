package cz.stavbau.backend.team.api.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Změna role člena")
public record UpdateMemberRoleRequest(
        @NotBlank
        @Schema(
                description = "Cílová role (CompanyRoleName). OWNER/SUPERADMIN nejsou touto cestou povoleny.",
                example = "MEMBER"
        )
        String role
) {}
