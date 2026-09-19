package com.oakpay.auth.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminDashboardService {

    private final AdminDashboardRepository repository;
    private final RestClient tradingClient;
    private final String internalSecret;

    public AdminDashboardService(
            AdminDashboardRepository repository,
            RestClient.Builder restClientBuilder,
            @Value("$" + "{oakpay.trading.base-url:http://localhost:8085}") String tradingBaseUrl,
            @Value("$" + "{oakpay.internal-secret}") String internalSecret) {
        this.repository = repository;
        this.tradingClient = restClientBuilder.baseUrl(tradingBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    @Transactional(readOnly = true)
    public AdminDashboard getDashboard() {
        DashboardDisputeCounts disputeCounts = getDisputeCounts();
        return new AdminDashboard(
                repository.countPendingKyc(),
                repository.countOpenQueries(),
                disputeCounts.activeDisputes(),
                disputeCounts.pendingResolutions()
        );
    }

    public AdminFinancialSummary getFinancialSummary() {
        try {
            AdminFinancialSummary result = tradingClient.get()
                    .uri("/api/v1/internal/admin/finance")
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(AdminFinancialSummary.class);
            if (result == null) {
                throw new IllegalStateException("Financial dashboard metrics were empty");
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Trading service financial dashboard request failed", e);
        }
    }

    public List<AdminFinancialSummary.P2PCommissionRecord> getCommissionRecords(
            String status,
            int limit) {
        try {
            int safeLimit = Math.min(Math.max(limit, 1), 200);
            AdminFinancialSummary.P2PCommissionRecord[] result = tradingClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/v1/internal/admin/finance/commissions")
                                .queryParam("limit", safeLimit);
                        if (status != null && !status.isBlank()) {
                            builder.queryParam("status", status.trim().toUpperCase());
                        }
                        return builder.build();
                    })
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(AdminFinancialSummary.P2PCommissionRecord[].class);
            return result == null ? List.of() : List.of(result);
        } catch (Exception e) {
            throw new IllegalStateException("Trading service commission request failed", e);
        }
    }

    public Map<String, BigDecimal> updateFee(String key, BigDecimal value, UUID actorId) {
        try {
            Map<?, ?> result = tradingClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/internal/admin/finance/fees/{key}")
                            .queryParam("value", value)
                            .build(key))
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header("X-OakPay-Admin-Actor", actorId == null ? "" : actorId.toString())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(Map.class);
            if (result == null) {
                throw new IllegalStateException("Fee setting update returned an empty response");
            }
            return result.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            e -> String.valueOf(e.getKey()),
                            e -> new BigDecimal(String.valueOf(e.getValue()))));
        } catch (Exception e) {
            throw new IllegalStateException("Trading service fee setting update failed", e);
        }
    }

    public AdminFinancialSummary.P2PCommissionRecord collectCommission(
            UUID tradeId,
            AdminFinancialSummary.CollectionRequest request,
            UUID actorId) {
        try {
            P2PCommissionResponse result = tradingClient.post()
                    .uri("/api/v1/internal/admin/finance/commissions/{tradeId}/collect", tradeId)
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header("X-OakPay-Admin-Actor", actorId == null ? "" : actorId.toString())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(P2PCommissionResponse.class);
            if (result == null) {
                throw new IllegalStateException("Commission collection returned an empty response");
            }
            return new AdminFinancialSummary.P2PCommissionRecord(
                    result.id(),
                    result.tradeId(),
                    result.payerId(),
                    result.fiatCurrency(),
                    result.fiatAmount(),
                    result.rate(),
                    result.commissionAmount(),
                    result.status(),
                    result.collectionReference(),
                    result.collectionMethod(),
                    result.collectedAt(),
                    result.createdAt(),
                    result.updatedAt());
        } catch (Exception e) {
            throw new IllegalStateException("Trading service commission collection failed", e);
        }
    }

    public List<AdminTransaction> getTransactions(int limit) {
        try {
            int safeLimit = Math.min(Math.max(limit, 1), 200);
            AdminTransaction[] result = tradingClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/internal/admin/trades")
                            .queryParam("limit", safeLimit).build())
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(AdminTransaction[].class);
            return result == null ? List.of() : List.of(result);
        } catch (Exception e) {
            throw new IllegalStateException("Trading service admin transaction request failed", e);
        }
    }

    private DashboardDisputeCounts getDisputeCounts() {
        try {
            DashboardDisputeCounts result = tradingClient.get()
                    .uri("/api/v1/internal/admin/dashboard/disputes")
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(DashboardDisputeCounts.class);
            if (result == null) throw new IllegalStateException("Trading dashboard metrics were empty");
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Trading service dashboard metrics request failed", e);
        }
    }

    public record DashboardDisputeCounts(long activeDisputes, long pendingResolutions) {}

    public record AdminTransaction(
            UUID id,
            UUID buyerId,
            UUID sellerId,
            UUID advertisementId,
            String asset,
            String fiatCurrency,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal fiatAmount,
            String paymentMethod,
            String status,
            String paymentReference,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    private record P2PCommissionResponse(
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
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}
}
