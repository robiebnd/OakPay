package com.oakpay.auth.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

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

    private DashboardDisputeCounts getDisputeCounts() {
        try {
            DashboardDisputeCounts result = tradingClient.get()
                    .uri("/api/v1/internal/admin/dashboard/disputes")
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(DashboardDisputeCounts.class);

            if (result == null) {
                throw new IllegalStateException("Trading dashboard metrics were empty");
            }

            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Trading service dashboard metrics request failed", e);
        }
    }

    public record DashboardDisputeCounts(long activeDisputes, long pendingResolutions) {}
}
