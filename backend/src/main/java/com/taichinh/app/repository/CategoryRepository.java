package com.taichinh.app.repository;

import com.taichinh.app.entity.Category;
import com.taichinh.app.enums.CategoryType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserIdAndDeletedAtIsNull(UUID userId, Sort sort);

    List<Category> findByUserIdAndTypeAndDeletedAtIsNull(UUID userId, CategoryType type, Sort sort);

    Page<Category> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Category> findByUserIdAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);

    Page<Category> findByUserIdAndTypeAndDeletedAtIsNull(UUID userId, CategoryType type, Pageable pageable);

    Page<Category> findByUserIdAndTypeAndDeletedAtIsNullAndNameContainingIgnoreCase(
            UUID userId,
            CategoryType type,
            String name,
            Pageable pageable);

    Optional<Category> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
