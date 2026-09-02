package com.oakpay.trading.api;

import com.oakpay.trading.order.OrderSide;
import com.oakpay.trading.order.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TradingDtos {
    private TradingDtos() {}

    public record CreateOrderRequest(
            OrderSide side,
            String baseCurrency,
            String quoteCurrency,
            BigDecimal price,
            BigDecimal quantity) {}

    public record OrderResponse(
            UUID id, UUID userId, OrderSide side, OrderStatus status,
            String baseCurrency, String quoteCurrency,
            BigDecimal price, BigDecimal quantity, BigDecimal remainingQuantity,
            LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record TradeResponse(
            UUID id, UUID buyOrderId, UUID sellOrderId,
            UUID buyerId, UUID sellerId,
            String baseCurrency, String quoteCurrency,
            BigDecimal price, BigDecimal quantity, BigDecimal grossValue,
            BigDecimal buyerFee, BigDecimal sellerFee,
            LocalDateTime createdAt) {}
}
