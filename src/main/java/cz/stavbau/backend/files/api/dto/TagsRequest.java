package cz.stavbau.backend.files.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TagsRequest(@NotNull List<String> tags) {}
