package com.taichinh.app.dto.category;

import com.taichinh.app.enums.CategoryType;
import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        String icon,
        String color,
        boolean defaultCategory,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
