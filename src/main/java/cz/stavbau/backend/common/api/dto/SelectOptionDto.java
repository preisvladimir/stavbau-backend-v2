// src/main/java/cz/stavbau/backend/common/api/dto/SelectOptionDto.java
package cz.stavbau.backend.common.api.dto;

import java.util.UUID;

/** Lehká volba pro selecty (value/label). */
public record SelectOptionDto(UUID value, String label) {}
