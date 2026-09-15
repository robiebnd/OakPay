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

    public List<AuditResponse> audit(UUID disputeId, String authorization) {
        return get("/api/v1/p2p/admin/disputes/" + disputeId + "/audit", authorization,
                new TypeReference<List<AuditResponse>>() {});
    }

    public DisputeResponse resolve(UUID disputeId, ResolveRequest request, String authorization) {
        return tradingClient.post()
                .uri("/api/v1/p2p/admin/disputes/" + disputeId + "/resolve")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("X-OakPay-Admin-Secret", adminSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DisputeResponse.class);
    }

    private <T> T get(String uri, String authorization, TypeReference<T> type) {
        try {
            String body = tradingClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .header("X-OakPay-Admin-Secret", adminSecret)
                    .retrieve()
                    .body(String.class);
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
