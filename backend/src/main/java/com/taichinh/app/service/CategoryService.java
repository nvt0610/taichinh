package com.taichinh.app.service;

import com.taichinh.app.dto.category.CategoryResponse;
import com.taichinh.app.dto.category.CreateCategoryRequest;
import com.taichinh.app.dto.category.UpdateCategoryRequest;
import com.taichinh.app.dto.common.ListQueryParams;
import com.taichinh.app.entity.Category;
import com.taichinh.app.enums.CategoryType;
import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import com.taichinh.app.repository.CategoryRepository;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "type", "createdAt", "updatedAt");

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        Category category = new Category(userId, request.name().trim(), request.type());
        category.setIcon(normalizeNullableText(request.icon()));
        category.setColor(normalizeNullableText(request.color()));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(UUID userId, CategoryType type, ListQueryParams queryParams) {
        String query = normalizeSearchQuery(queryParams.getQ());
        var pageable = queryParams.toPageable(ALLOWED_SORT_FIELDS, "name", Sort.Direction.ASC);
        Page<Category> page;

        if (type == null && query == null) {
            page = categoryRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        } else if (type == null) {
            page = categoryRepository.findByUserIdAndDeletedAtIsNullAndNameContainingIgnoreCase(userId, query, pageable);
        } else if (query == null) {
            page = categoryRepository.findByUserIdAndTypeAndDeletedAtIsNull(userId, type, pageable);
        } else {
            page = categoryRepository.findByUserIdAndTypeAndDeletedAtIsNullAndNameContainingIgnoreCase(
                    userId,
                    type,
                    query,
                    pageable);
        }

        return page
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID userId, UUID categoryId) {
        return toResponse(findActiveCategory(userId, categoryId));
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        Category category = findActiveCategory(userId, categoryId);
        category.setName(request.name().trim());
        category.setType(request.type());
        category.setIcon(normalizeNullableText(request.icon()));
        category.setColor(normalizeNullableText(request.color()));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void softDelete(UUID userId, UUID categoryId) {
        Category category = findActiveCategory(userId, categoryId);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    private Category findActiveCategory(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found."));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.isDefaultCategory(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    private String normalizeSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
