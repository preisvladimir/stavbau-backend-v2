// src/main/java/cz/stavbau/backend/common/api/PageResponse.java
package cz.stavbau.backend.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Konzistentní obal pro stránkované odpovědi v REST API.
 * Použití: PageResponse.of(page) nebo PageResponse.of(page.map(mapper))
 */
@Schema(name = "PageResponse")
public record PageResponse<T>(
        @Schema(description = "Položky aktuální stránky")
        @JsonProperty("items")
        List<T> items,

        @Schema(description = "Index stránky (0-based)")
        @JsonProperty("page")
        int page,

        @Schema(description = "Počet položek na stránku")
        @JsonProperty("size")
        int size,

        @Schema(description = "Celkový počet položek přes všechny stránky")
        @JsonProperty("totalElements")
        long totalElements,

        @Schema(description = "Celkový počet stránek")
        @JsonProperty("totalPages")
        int totalPages,

        @Schema(description = "Existuje následující stránka?")
        @JsonProperty("hasNext")
        boolean hasNext,

        @Schema(description = "Existuje předchozí stránka?")
        @JsonProperty("hasPrevious")
        boolean hasPrevious
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / (double) size) : 1;
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrevious = page > 0 && totalPages > 0;
        return new PageResponse<>(items, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }

    /**
     * Pohodlné mapování obsahu bez ztráty metadat stránkování.
     */
    public <R> PageResponse<R> map(Function<T, R> mapper) {
        var mapped = this.items.stream().map(mapper).toList();
        return new PageResponse<>(
                mapped,
                this.page,
                this.size,
                this.totalElements,
                this.totalPages,
                this.hasNext,
                this.hasPrevious
        );
    }
}
