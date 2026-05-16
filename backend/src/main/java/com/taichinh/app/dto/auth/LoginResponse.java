package com.taichinh.app.dto.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LoginResponse(
        UUID id,
        String username,
        String email,
        List<String> roles,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        LocalDateTime lastLoginAt) {
}
