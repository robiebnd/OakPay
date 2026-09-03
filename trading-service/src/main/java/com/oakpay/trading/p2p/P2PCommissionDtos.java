package com.oakpay.trading.p2p;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PCommissionDtos {
    private P2PCommissionDtos() {}

    public record Response(
            UUID id,
            UUID tradeId,
            UUID payerId,
            String fiatCurrency,
            BigDecimal fiatAmount,
            BigDecimal rate,
            BigDecimal commissionAmount,
            P2PCommissionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        static Response from(P2PCommission c) {
            return new Response(c.getId(), c.getTradeId(), c.getPayerId(), c.getFiatCurrency(),
                    c.getFiatAmount(), c.getRate(), c.getCommissionAmount(), c.getStatus(),
                    c.getCreatedAt(), c.getUpdatedAt());
        }
    }
}
