package com.rentalapp.common.api;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PaginatedResponse<T> {
    private final List<T> items;
    private final int currentPage;
    private final int perPage;
    private final int totalPages;
    private final long totalItems;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final String sort;

    public static <T> PaginatedResponse<T> from(Page<T> page, int requestedPage) {
        return from(page, requestedPage, null);
    }

    public static <T> PaginatedResponse<T> from(Page<T> page, int requestedPage, String sort) {
        return PaginatedResponse.<T>builder()
                .items(page.getContent())
                .currentPage(requestedPage)
                .perPage(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .sort(sort)
                .build();
    }
}
