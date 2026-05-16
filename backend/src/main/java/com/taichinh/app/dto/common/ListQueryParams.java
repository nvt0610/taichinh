package com.taichinh.app.dto.common;

import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ListQueryParams {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Min(1)
    private int page = DEFAULT_PAGE;

    @Min(1)
    @Max(MAX_SIZE)
    private int size = DEFAULT_SIZE;

    private String q;

    private String sort;

    public Pageable toPageable(Set<String> allowedSortFields, String defaultSortField, Sort.Direction defaultDirection) {
        validatePageAndSize();
        Sort resolvedSort = resolveSort(allowedSortFields, defaultSortField, defaultDirection);
        return PageRequest.of(page - 1, size, resolvedSort);
    }

    private void validatePageAndSize() {
        if (page < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Page must be greater than or equal to 1.");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Size must be between 1 and 100.");
        }
    }

    private Sort resolveSort(Set<String> allowedSortFields, String defaultSortField, Sort.Direction defaultDirection) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(defaultDirection, defaultSortField);
        }

        String[] parts = sort.split(",", -1);
        String field = parts[0].trim();
        Sort.Direction direction = parts.length > 1 ? parseDirection(parts[1]) : defaultDirection;

        if (field.isBlank() || !allowedSortFields.contains(field)) {
            throw new BusinessException(ErrorCode.INVALID_SORT, "Sort field is not supported: " + field);
        }

        return Sort.by(direction, field);
    }

    private Sort.Direction parseDirection(String rawDirection) {
        String normalized = rawDirection.trim().toUpperCase(Locale.ROOT);
        if ("ASC".equals(normalized)) {
            return Sort.Direction.ASC;
        }
        if ("DESC".equals(normalized)) {
            return Sort.Direction.DESC;
        }
        throw new BusinessException(ErrorCode.INVALID_SORT, "Sort direction must be asc or desc.");
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
