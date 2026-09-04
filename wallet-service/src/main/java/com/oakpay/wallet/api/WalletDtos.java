package com.oakpay.wallet.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class WalletDtos {
    private WalletDtos() {}

    public record CreateWalletRequest(String currency) {}

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
