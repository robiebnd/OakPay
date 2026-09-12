package com.oakpay.auth.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class AdminKycDtos {
    private AdminKycDtos() {}
    public record QueueItem(UUID kycId, UUID userId, String clientName, String email, String country, String status, LocalDateTime submittedAt, List<KycDtos.DocumentResponse> documents) {}
    public record QueueSummary(long pending, long verified, long rejected) {}
    public record DecisionRequest(String reason) {}
    public record DecisionResponse(UUID kycId, String status, String reason, LocalDateTime reviewedAt) {}
}