package com.oakpay.auth.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResolutionCentreService {
    private final RestClient tradingClient;
    private final ObjectMapper objectMapper;
    private final String adminSecret;

    public ResolutionCentreService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${oakpay.trading.base-url:http://localhost:8085}") String tradingBaseUrl,
            @Value("${oakpay.admin.dispute-secret}") String adminSecret) {
        this.tradingClient = restClientBuilder.baseUrl(tradingBaseUrl).build();
        this.objectMapper = objectMapper;
        this.adminSecret = adminSecret;
    }

    public List<DisputeResponse> disputes(String authorization) {
        return get("/api/v1/p2p/admin/disputes", authorization,
                new TypeReference<List<DisputeResponse>>() {});
    }

    public DisputeResponse dispute(UUID disputeId, String authorization) {
        return get("/api/v1/p2p/admin/disputes/" + disputeId, authorization,
                new TypeReference<DisputeResponse>() {});
    }

    public List<AuditResponse> audit(UUID disputeId, String authorization) {
        return get("/api/v1/p2p/admin/disputes/" + disputeId + "/audit", authorization,
                new TypeReference<List<AuditResponse>>() {});
    }

    public DisputeResponse resolve(UUID disputeId, ResolveRequest request, String authorization) {
        var builder = tradingClient.post()
                .uri("/api/v1/p2p/admin/disputes/" + disputeId + "/resolve")
                .header("X-OakPay-Admin-Secret", adminSecret)
                .header(HttpHeaders.ACCEPT, "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request);

        if (authorization != null && !authorization.isBlank()) {
            builder.header(HttpHeaders.AUTHORIZATION, authorization);
        }

        try {
            return builder.retrieve().body(DisputeResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("Trading service dispute resolution request failed", e);
        }
    }

    private <T> T get(String uri, String authorization, TypeReference<T> type) {
        try {
            var builder = tradingClient.get()
                    .uri(uri)
                    .header("X-OakPay-Admin-Secret", adminSecret)
                    .header(HttpHeaders.ACCEPT, "application/json");

            if (authorization != null && !authorization.isBlank()) {
                builder.header(HttpHeaders.AUTHORIZATION, authorization);
            }

            String body = builder.retrieve().body(String.class);
            return objectMapper.readValue(body, type);
        } catch (Exception e) {
            throw new IllegalStateException("Trading service dispute request failed", e);
        }
    }

    public record ResolveRequest(String resolution, String note) {}
    public record DisputeResponse(UUID id, UUID tradeId, UUID openedBy, String reason, String evidence,
                                  String status, String resolution, String resolutionNote, UUID resolvedBy,
                                  LocalDateTime resolvedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record AuditResponse(UUID id, UUID disputeId, UUID tradeId, UUID actorId,
                                String eventType, String note, LocalDateTime createdAt) {}
}
