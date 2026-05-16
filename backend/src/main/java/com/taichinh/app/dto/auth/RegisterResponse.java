package com.taichinh.app.dto.auth;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt) {
}
