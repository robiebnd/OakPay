package com.oakpay.auth.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminFinancialSummary(
        Map<String, BigDecimal> currentFees,
        long executedTrades,
        long commissionRecords,
        long collectedCommissionRecords,
        List<SpotFeeSummary> spotFees,
        List<P2PCommissionSummary> p2pCommissions) {

    public record SpotFeeSummary(
            String quoteCurrency,
            long tradeCount,
            BigDecimal grossVolume,
            BigDecimal buyerFees,
            BigDecimal sellerFees) {
        public BigDecimal totalFees() {
            return money(buyerFees).add(money(sellerFees));
        }
    }

    public record P2PCommissionSummary(
            String fiatCurrency,
            long commissionCount,
            BigDecimal assessed,
            BigDecimal collected,
            BigDecimal outstanding,
            BigDecimal waived) {}

    public record P2PCommissionRecord(
            UUID id,
            UUID tradeId,
            UUID payerId,
            String fiatCurrency,
            BigDecimal fiatAmount,
            BigDecimal rate,
            BigDecimal commissionAmount,
            String status,
            String collectionReference,
            String collectionMethod,
            LocalDateTime collectedAt,
            LocalDateTime createdAt) {}

    public record CollectionRequest(String collectionReference, String collectionMethod) {}

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
