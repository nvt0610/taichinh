package com.taichinh.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username must not be blank.")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username may only contain letters, numbers, dot, underscore, and hyphen.")
        String username,

        @NotBlank(message = "Email must not be blank.")
        @Email(message = "Email must be a valid email address.")
        @Size(max = 255, message = "Email must not exceed 255 characters.")
        String email,

        @NotBlank(message = "Password must not be blank.")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, and one number.")
        String password) {
}
