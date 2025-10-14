package cz.stavbau.backend.features.files.api.dto;

import cz.stavbau.backend.features.files.model.LinkTarget;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkRequest(@NotNull LinkTarget targetType, @NotNull UUID targetId) {}
