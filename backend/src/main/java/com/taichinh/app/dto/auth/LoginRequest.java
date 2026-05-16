package com.taichinh.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username or email must not be blank.")
        @Size(max = 255, message = "Username or email must not exceed 255 characters.")
        String usernameOrEmail,

        @NotBlank(message = "Password must not be blank.")
        @Size(max = 100, message = "Password must not exceed 100 characters.")
        String password) {
}
