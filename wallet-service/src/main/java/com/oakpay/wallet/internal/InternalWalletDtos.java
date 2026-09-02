package com.oakpay.wallet.internal;

import java.math.BigDecimal;
import java.util.UUID;

public final class InternalWalletDtos {
    private InternalWalletDtos() {}

    public record MutationRequest(UUID userId, BigDecimal amount, String reference) {}

    public record SettlementRequest(
            UUID buyerId,
            UUID sellerId,
            String baseCurrency,
            String quoteCurrency,
            BigDecimal baseAmount,
            BigDecimal quoteAmount,
            BigDecimal buyerFee,
            BigDecimal sellerFee,
            String reference) {}

    public record EscrowReleaseRequest(UUID sellerId, UUID buyerId, String asset, BigDecimal amount) {}
}
