package com.oakpay.trading.p2p;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PTradeDtos {
    private P2PTradeDtos() {}

    public record CreateRequest(
            UUID buyerId,
            String asset,
            String fiatCurrency,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String paymentMethod,
            Integer expiryMinutes) {}

    public record PaymentRequest(String paymentReference, String paymentNote) {}

    public record TradeResponse(
            UUID id,
            UUID buyerId,
            UUID sellerId,
            String asset,
            String fiatCurrency,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal fiatAmount,
            String paymentMethod,
            P2PTradeStatus status,
            String paymentReference,
            String paymentNote,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        static TradeResponse from(P2PTrade t) {
            return new TradeResponse(t.getId(), t.getBuyerId(), t.getSellerId(), t.getAsset(), t.getFiatCurrency(),
                    t.getQuantity(), t.getUnitPrice(), t.getFiatAmount(), t.getPaymentMethod(), t.getStatus(),
                    t.getPaymentReference(), t.getPaymentNote(), t.getExpiresAt(), t.getCreatedAt(), t.getUpdatedAt());
        }
    }
}
