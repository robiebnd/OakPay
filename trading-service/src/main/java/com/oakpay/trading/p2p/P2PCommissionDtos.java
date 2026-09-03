package com.oakpay.trading.p2p;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PCommissionDtos {
    private P2PCommissionDtos() {}

    public record CollectionRequest(
            @NotBlank String collectionReference,
            @NotBlank String collectionMethod) {}

    public record Response(
            UUID id,
            UUID tradeId,
            UUID payerId,
            String fiatCurrency,
            BigDecimal fiatAmount,
            BigDecimal rate,
            BigDecimal commissionAmount,
            P2PCommissionStatus status,
            String collectionReference,
            String collectionMethod,
            LocalDateTime collectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        static Response from(P2PCommission c) {
            return new Response(c.getId(), c.getTradeId(), c.getPayerId(), c.getFiatCurrency(),
                    c.getFiatAmount(), c.getRate(), c.getCommissionAmount(), c.getStatus(),
                    c.getCollectionReference(), c.getCollectionMethod(), c.getCollectedAt(),
                    c.getCreatedAt(), c.getUpdatedAt());
        }
    }
}
