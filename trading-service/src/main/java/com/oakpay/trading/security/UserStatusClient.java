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
            @Value("${oakpay.auth.base-url:http://localhost:8083}") String authBaseUrl,
            @Value("${oakpay.internal-secret:oakpay-internal-development-secret-change-before-production}") String internalSecret) {
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

    public String displayName(UUID userId) {
        if (userId == null) return "PayOak user";
        try {
            UserProfileResponse response = client.get()
                    .uri("/api/v1/internal/users/{userId}/profile", userId)
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(UserProfileResponse.class);
            if (response == null || response.displayName() == null || response.displayName().isBlank()) {
                return "PayOak user";
            }
            return response.displayName();
        } catch (RuntimeException ex) {
            return "PayOak user";
        }
    }

    private record ActiveUserResponse(UUID userId, boolean active) {}
    private record UserProfileResponse(UUID userId, String displayName) {}
}
