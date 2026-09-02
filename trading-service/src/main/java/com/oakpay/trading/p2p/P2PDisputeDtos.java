package com.oakpay.trading.p2p;

import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PDisputeDtos {
    private P2PDisputeDtos() {}

    public record OpenRequest(String reason, String evidence) {}
    public record ResolveRequest(DisputeResolution resolution, String note) {}

    public record DisputeResponse(
            UUID id, UUID tradeId, UUID openedBy, String reason, String evidence,
            DisputeStatus status, DisputeResolution resolution, String resolutionNote,
            UUID resolvedBy, LocalDateTime resolvedAt, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        static DisputeResponse from(P2PDispute d) {
            return new DisputeResponse(d.getId(), d.getTradeId(), d.getOpenedBy(), d.getReason(), d.getEvidence(),
                    d.getStatus(), d.getResolution(), d.getResolutionNote(), d.getResolvedBy(),
                    d.getResolvedAt(), d.getCreatedAt(), d.getUpdatedAt());
        }
    }

    public record AuditResponse(UUID id, UUID disputeId, UUID tradeId, UUID actorId,
                                String eventType, String note, LocalDateTime createdAt) {
        static AuditResponse from(P2PDisputeAudit a) {
            return new AuditResponse(a.getId(), a.getDisputeId(), a.getTradeId(), a.getActorId(),
                    a.getEventType(), a.getNote(), a.getCreatedAt());
        }
    }
}
