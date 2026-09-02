package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_disputes", indexes = {
        @Index(name = "idx_p2p_disputes_trade", columnList = "trade_id"),
        @Index(name = "idx_p2p_disputes_status", columnList = "status")
})
public class P2PDispute {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "trade_id", nullable = false, updatable = false, unique = true)
    private UUID tradeId;

    @Column(name = "opened_by", nullable = false, updatable = false)
    private UUID openedBy;

    @Column(nullable = false, length = 40)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DisputeResolution resolution;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = DisputeStatus.OPEN;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID v) { tradeId = v; }
    public UUID getOpenedBy() { return openedBy; }
    public void setOpenedBy(UUID v) { openedBy = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { reason = v; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String v) { evidence = v; }
    public DisputeStatus getStatus() { return status; }
    public void setStatus(DisputeStatus v) { status = v; }
    public DisputeResolution getResolution() { return resolution; }
    public void setResolution(DisputeResolution v) { resolution = v; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String v) { resolutionNote = v; }
    public UUID getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(UUID v) { resolvedBy = v; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime v) { resolvedAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
