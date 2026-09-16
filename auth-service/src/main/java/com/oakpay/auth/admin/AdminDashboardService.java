package com.oakpay.auth.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminDashboardService {

    private final AdminDashboardRepository repository;
    private final RestClient tradingClient;
    private final String internalSecret;

    public AdminDashboardService(
            AdminDashboardRepository repository,
            RestClient.Builder restClientBuilder,
            @Value("${oakpay.trading.base-url:http://localhost:8085}") String tradingBaseUrl,
            @Value("${oakpay.internal-secret}") String internalSecret) {
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
}
