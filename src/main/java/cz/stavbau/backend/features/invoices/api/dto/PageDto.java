package cz.stavbau.backend.features.invoices.api.dto;

import java.util.List;

public record PageDto<T>(List<T> items, long total, int page, int size) {}
