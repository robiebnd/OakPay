package com.oakpay.trading.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.UUID;

@Component
public class KycStatusClient {
    private final RestClient client;
    private final String internalSecret;

    public KycStatusClient(
            @Value("${oakpay.auth.base-url:http://localhost:8083}") String authBaseUrl,
            @Value("${oakpay.internal-secret:oakpay-internal-development-secret-change-before-production}") String internalSecret) {
        this.client = RestClient.builder().baseUrl(authBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    public String getStatus(UUID userId) {
        if (userId == null) return "NOT_STARTED";
        try {
            KycStatusResponse response = client.get()
                    .uri("/api/v1/internal/users/{userId}/kyc-status", userId)
                    .header("X-OakPay-Internal-Secret", internalSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(KycStatusResponse.class);
            return response == null || response.status() == null
                    ? "NOT_STARTED"
                    : response.status().trim().toUpperCase(Locale.ROOT);
        } catch (RuntimeException ex) {
            // Fail closed: if KYC status cannot be verified, apply the unverified limit.
            return "NOT_STARTED";
        }
    }

    public boolean isVerified(UUID userId) {
        return "VERIFIED".equals(getStatus(userId));
    }

    private record KycStatusResponse(UUID userId, String status) {}
}
