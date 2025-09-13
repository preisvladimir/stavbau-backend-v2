package cz.stavbau.backend.files.api.dto;

import cz.stavbau.backend.files.model.LinkTarget;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkRequest(@NotNull LinkTarget targetType, @NotNull UUID targetId) {}
