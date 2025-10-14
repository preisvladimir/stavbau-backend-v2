package cz.stavbau.backend.features.members.dto.read;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Seznam členů firmy")
public record MembersPageDto(
        List<MemberSummaryDto> items,
        int page,            // 0-based
        int size,
        long totalElements,
        int totalPages,
        String sort          // např. "createdAt,desc"
) {}
