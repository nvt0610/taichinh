package com.taichinh.app.controller;

import com.taichinh.app.dto.category.CategoryResponse;
import com.taichinh.app.dto.category.CreateCategoryRequest;
import com.taichinh.app.dto.category.UpdateCategoryRequest;
import com.taichinh.app.dto.common.ApiResponse;
import com.taichinh.app.dto.common.ListQueryParams;
import com.taichinh.app.dto.common.PaginationResponse;
import com.taichinh.app.enums.CategoryType;
import com.taichinh.app.security.AuthenticatedUserProvider;
import com.taichinh.app.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CategoryController(CategoryService categoryService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.categoryService = categoryService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            Authentication authentication,
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.create(authenticatedUserProvider.getUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(
            Authentication authentication,
            @RequestParam(required = false) CategoryType type,
            @Valid @ModelAttribute ListQueryParams queryParams) {
        Page<CategoryResponse> page = categoryService.list(
                authenticatedUserProvider.getUserId(authentication),
                type,
                queryParams);
        return ResponseEntity.ok(ApiResponse.success(
                "Categories retrieved successfully.",
                page.getContent(),
                PaginationResponse.from(page)));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            Authentication authentication,
            @PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.getById(
                authenticatedUserProvider.getUserId(authentication),
                categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully.", response));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            Authentication authentication,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.update(
                authenticatedUserProvider.getUserId(authentication),
                categoryId,
                request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully.", response));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable UUID categoryId) {
        categoryService.softDelete(authenticatedUserProvider.getUserId(authentication), categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully.", null));
    }
}
