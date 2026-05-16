package com.taichinh.app.dto.common;

import org.springframework.data.domain.Page;

public record PaginationResponse(
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {

    public static PaginationResponse from(Page<?> page) {
        return new PaginationResponse(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}
