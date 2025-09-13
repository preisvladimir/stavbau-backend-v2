package cz.stavbau.backend.files.api.dto;

import java.time.Instant;
import java.util.UUID;

public record StoredFileResponse(
        UUID id, UUID companyId, UUID uploaderId,
        String originalName, String mimeType, long sizeBytes,
        String sha256, String storageKey, Instant createdAt
) {}
