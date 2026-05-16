package com.taichinh.app.dto.common;

public record FieldErrorResponse(
        String field,
        String message) {
}
