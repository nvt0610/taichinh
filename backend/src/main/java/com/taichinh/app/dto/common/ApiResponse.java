package com.taichinh.app.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        PaginationResponse pagination,
        ErrorResponse error,
        Instant timestamp) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data, PaginationResponse pagination) {
        return new ApiResponse<>(true, message, data, pagination, null, Instant.now());
    }

    public static ApiResponse<Void> error(String message, ErrorResponse error) {
        return new ApiResponse<>(false, message, null, null, error, Instant.now());
    }
}
