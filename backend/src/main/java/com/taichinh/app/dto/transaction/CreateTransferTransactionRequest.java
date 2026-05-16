package com.taichinh.app.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTransferTransactionRequest(
        @NotNull(message = "Source wallet ID is required.")
        UUID sourceWalletId,

        @NotNull(message = "Destination wallet ID is required.")
        UUID destinationWalletId,

        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0.")
        BigDecimal amount,

        @NotBlank(message = "Title is required.")
        @Size(max = 255, message = "Title must be at most 255 characters.")
        String title,

        @Size(max = 5000, message = "Note must be at most 5000 characters.")
        String note,

        @NotNull(message = "Transaction date is required.")
        LocalDateTime transactionDate) {
}
