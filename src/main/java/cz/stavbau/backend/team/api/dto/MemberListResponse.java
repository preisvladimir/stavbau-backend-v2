package cz.stavbau.backend.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Seznam členů firmy")
public record MemberListResponse(
        List<MemberDto> items,
        int total
) {}
