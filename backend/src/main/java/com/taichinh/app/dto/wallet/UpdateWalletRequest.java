package com.taichinh.app.dto.wallet;

import com.taichinh.app.enums.WalletType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWalletRequest(
        @NotBlank(message = "Wallet name is required.")
        @Size(max = 100, message = "Wallet name must be at most 100 characters.")
        String name,

        @NotNull(message = "Wallet type is required.")
        WalletType type,

        @Size(max = 1000, message = "Description must be at most 1000 characters.")
        String description) {
}
