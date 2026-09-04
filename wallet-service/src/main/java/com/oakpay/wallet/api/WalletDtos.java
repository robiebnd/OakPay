package com.oakpay.wallet.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class WalletDtos {
    private WalletDtos() {}

    public record CreateWalletRequest(String currency) {}

    public record DepositRequest(
            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.000000000000000001", message = "Amount must be greater than zero")
            BigDecimal amount,
            @NotBlank(message = "Reference is required")
            String reference) {}

    public record WalletResponse(
            UUID id,
            UUID userId,
            String currency,
            BigDecimal availableBalance,
            BigDecimal lockedBalance,
            BigDecimal totalBalance,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}
}
