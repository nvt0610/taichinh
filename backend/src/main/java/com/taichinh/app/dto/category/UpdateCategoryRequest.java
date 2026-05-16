package com.taichinh.app.dto.category;

import com.taichinh.app.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank(message = "Category name is required.")
        @Size(max = 100, message = "Category name must be at most 100 characters.")
        String name,

        @NotNull(message = "Category type is required.")
        CategoryType type,

        @Size(max = 100, message = "Icon must be at most 100 characters.")
        String icon,

        @Size(max = 20, message = "Color must be at most 20 characters.")
        String color) {
}
