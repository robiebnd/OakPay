package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_dispute_audit", indexes = {
        @Index(name = "idx_p2p_dispute_audit_dispute", columnList = "dispute_id"),
        @Index(name = "idx_p2p_dispute_audit_created", columnList = "created_at")
})
public class P2PDisputeAudit {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dispute_id", nullable = false, updatable = false)
    private UUID disputeId;

    @Column(name = "trade_id", nullable = false, updatable = false)
    private UUID tradeId;

    @Column(name = "actor_id", nullable = false, updatable = false)
    private UUID actorId;

    @Column(name = "event_type", nullable = false, length = 40, updatable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getDisputeId() { return disputeId; }
    public void setDisputeId(UUID v) { disputeId = v; }
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID v) { tradeId = v; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID v) { actorId = v; }
    public String getEventType() { return eventType; }
    public void setEventType(String v) { eventType = v; }
    public String getNote() { return note; }
    public void setNote(String v) { note = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
