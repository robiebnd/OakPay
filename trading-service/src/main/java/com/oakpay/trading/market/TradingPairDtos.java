package com.oakpay.trading.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TradingPairDtos {
    private TradingPairDtos() {}

    public record CreateRequest(
            String symbol,
            String baseCurrency,
            String quoteCurrency,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal priceTickSize,
            BigDecimal minQuantity,
            BigDecimal quantityStepSize) {}

    public record UpdateRequest(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal priceTickSize,
            BigDecimal minQuantity,
            BigDecimal quantityStepSize,
            TradingPairStatus status) {}

    public record PairResponse(
            UUID id,
            String symbol,
            String baseCurrency,
            String quoteCurrency,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal priceTickSize,
            BigDecimal minQuantity,
            BigDecimal quantityStepSize,
            TradingPairStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}
}
