package com.taichinh.app.dto.dashboard;

import com.taichinh.app.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DashboardRecentTransactionResponse(
        UUID id,
        UUID walletId,
        String walletName,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        String title,
        LocalDateTime transactionDate) {
}
