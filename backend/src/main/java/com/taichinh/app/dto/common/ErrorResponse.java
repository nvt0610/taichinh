package com.taichinh.app.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taichinh.app.exception.ErrorCode;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        ErrorCode code,
        List<FieldErrorResponse> details) {

    public ErrorResponse {
        if (details != null) {
            details = List.copyOf(details);
        }
    }

    public static ErrorResponse of(ErrorCode code) {
        return new ErrorResponse(code, null);
    }

    public static ErrorResponse of(ErrorCode code, List<FieldErrorResponse> details) {
        return new ErrorResponse(code, details);
    }
}
