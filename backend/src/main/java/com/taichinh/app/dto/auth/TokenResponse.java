package com.taichinh.app.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
