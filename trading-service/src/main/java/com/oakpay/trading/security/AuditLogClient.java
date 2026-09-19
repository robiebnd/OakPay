package com.oakpay.trading.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class AuditLogClient {
    private final RestClient client;
    private final String internalSecret;

    public AuditLogClient(
            @Value("${oakpay.auth.base-url:http://localhost:8083}") String authBaseUrl,
            @Value("${oakpay.internal-secret:oakpay-internal-development-secret-change-before-production}") String internalSecret) {
        this.client = RestClient.builder().baseUrl(authBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    public void record(UUID actorUserId, String action, String resourceType, String resourceId,
                       String outcome, String metadata) {
        try {
            client.post()
                    .uri("/api/v1/internal/audit-logs")
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuditRequest(actorUserId, "ADMIN", action, resourceType, resourceId, outcome, null, metadata))
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Audit failures must not roll back a completed financial operation.
            // Operational monitoring should alert on unavailable audit storage.
        }
    }

    private record AuditRequest(UUID actorUserId, String actorType, String action,
                                String resourceType, String resourceId, String outcome,
                                String ipAddress, String metadata) {}
}
