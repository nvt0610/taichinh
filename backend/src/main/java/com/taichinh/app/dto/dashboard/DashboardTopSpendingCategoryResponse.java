package com.taichinh.app.dto.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record DashboardTopSpendingCategoryResponse(
        UUID categoryId,
        String categoryName,
        String icon,
        String color,
        BigDecimal totalAmount,
        long transactionCount) {
}
