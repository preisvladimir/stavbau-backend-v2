package cz.stavbau.backend.features.members.dto.read;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Reprezentace člena týmu")
public record MemberDto(
        UUID memberId,
        UUID userId,
        String email,
        String role,
        String firstName,
        String lastName,
        String phone,
        @Schema(description = "CREATED|INVITED", example = "INVITED")
        String status,
        String state
) {}
