package com.taichinh.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token must not be blank.")
        @Size(max = 500, message = "Refresh token must not exceed 500 characters.")
        String refreshToken) {
}
