package com.oakpay.trading.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationClient {
    private final RestClient restClient;
    private final String internalSecret;

    public NotificationClient(
            RestClient.Builder builder,
            @Value("${oakpay.auth.base-url:http://localhost:8083}") String authBaseUrl,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.restClient = builder.baseUrl(authBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    public void send(UUID userId, String type, String title, String message, Map<String, Object> data) {
        if (userId == null) return;
        try {
            String json = data == null ? null : new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
            restClient.post()
                    .uri("/api/v1/internal/notifications")
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "userId", userId,
                            "type", type,
                            "title", title,
                            "message", message,
                            "data", json == null ? "" : json))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
            // Notifications are best-effort and must never break a trade.
        }
    }
}
