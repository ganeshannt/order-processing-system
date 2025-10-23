package io.ganeshannt.asm.ops.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {

    @Schema(description = "List of items in current page")
    private List<T> content;

    @Schema(description = "Current page number (1-indexed)", example = "1")
    private int pageNumber;

    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;

    @Schema(description = "Total number of items across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;

    /**
     * Factory method to create PagedResponse from Spring Page
     *
     * Converts 0-indexed Spring page to 1-indexed API response
     * Excludes first, last, empty flags
     *
     * Clients can derive these values:
     * - isFirst: pageNumber == 1
     * - isLast: pageNumber == totalPages
     * - isEmpty: content.size() == 0
     *
     * @param page Spring Data Page (0-indexed)
     * @return PagedResponse with 1-indexed page number
     */
    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber() + 1)  // Convert 0-indexed to 1-indexed
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
