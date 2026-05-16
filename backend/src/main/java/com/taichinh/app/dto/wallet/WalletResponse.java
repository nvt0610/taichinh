package com.taichinh.app.dto.wallet;

import com.taichinh.app.enums.WalletType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String name,
        WalletType type,
        BigDecimal balance,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
