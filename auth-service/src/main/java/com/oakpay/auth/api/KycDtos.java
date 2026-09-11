package com.oakpay.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class KycDtos {
    private KycDtos() {}

    public record DocumentRequest(
            @NotBlank @Size(max = 40) String documentType,
            @Size(max = 100) String documentNumber) {}

    public record DocumentResponse(
            UUID id,
            String documentType,
            String documentNumberMasked,
            String status,
            String rejectionReason,
            boolean frontUploaded,
            boolean backUploaded,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    public record KycResponse(
            UUID id,
            UUID userId,
            String status,
            String rejectionReason,
            LocalDateTime submittedAt,
            LocalDateTime reviewedAt,
            List<DocumentResponse> documents) {}
}
