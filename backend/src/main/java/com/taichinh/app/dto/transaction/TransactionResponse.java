package com.taichinh.app.dto.transaction;

import com.taichinh.app.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount,
        String title,
        String note,
        LocalDateTime transactionDate,
        UUID referenceTransactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
