package cz.stavbau.backend.team.api.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Změna role člena")
public record UpdateMemberRequest(
        @NotBlank @Schema(allowableValues = {"ADMIN","MEMBER"}, example = "ADMIN")
        String role
) {}
