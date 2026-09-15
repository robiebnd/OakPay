package com.oakpay.trading.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class UserStatusClient {
    private final RestClient client;
    private final String internalSecret;

    public UserStatusClient(
            @Value("${oakpay.auth.base-url}") String authBaseUrl,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.client = RestClient.builder().baseUrl(authBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    public boolean isActive(UUID userId) {
        if (userId == null) return false;
        try {
            ActiveUserResponse response = client.get()
                    .uri("/api/v1/internal/users/{userId}/active", userId)
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ActiveUserResponse.class);
            return response != null && response.active();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private record ActiveUserResponse(UUID userId, boolean active) {}
}
