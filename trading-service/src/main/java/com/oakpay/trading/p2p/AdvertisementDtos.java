package com.oakpay.trading.p2p;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class AdvertisementDtos {
    private AdvertisementDtos() {}

    public record CreateRequest(
            OrderSide side,
            String asset,
            String fiatCurrency,
            BigDecimal price,
            BigDecimal totalQuantity,
            BigDecimal minQuantity,
            BigDecimal maxQuantity,
            String paymentMethods,
            String terms) {}

    public record UpdateRequest(
            BigDecimal price,
            BigDecimal minQuantity,
            BigDecimal maxQuantity,
            String paymentMethods,
            String terms) {}

    public record TakeRequest(
            BigDecimal quantity,
            String paymentMethod,
            Integer expiryMinutes) {}

    public record AdResponse(
            UUID id,
            UUID ownerId,
            OrderSide side,
            String asset,
            String fiatCurrency,
            BigDecimal price,
            BigDecimal totalQuantity,
            BigDecimal availableQuantity,
            BigDecimal minQuantity,
            BigDecimal maxQuantity,
            String paymentMethods,
            String terms,
            AdStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        static AdResponse from(Advertisement a) {
            return new AdResponse(a.getId(), a.getOwnerId(), a.getSide(), a.getAsset(), a.getFiatCurrency(),
                    a.getPrice(), a.getTotalQuantity(), a.getAvailableQuantity(), a.getMinQuantity(),
                    a.getMaxQuantity(), a.getPaymentMethods(), a.getTerms(), a.getStatus(),
                    a.getCreatedAt(), a.getUpdatedAt());
        }
    }
}
