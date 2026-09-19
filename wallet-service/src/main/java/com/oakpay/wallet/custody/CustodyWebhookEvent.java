package com.oakpay.wallet.custody;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "custody_webhook_events", uniqueConstraints = {
        @UniqueConstraint(name = "uk_custody_webhook_provider_event", columnNames = {"provider_name", "event_id"})
})
public class CustodyWebhookEvent {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "provider_name", nullable = false, length = 80, updatable = false)
    private String providerName;

    @Column(name = "event_id", nullable = false, length = 150, updatable = false)
    private String eventId;

    @Column(name = "payload_hash", nullable = false, length = 64, updatable = false)
    private String payloadHash;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustodyWebhookEventStatus status;

    @Column(name = "deposit_id")
    private UUID depositId;

    @Column(name = "withdrawal_operation_id")
    private UUID withdrawalOperationId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = CustodyWebhookEventStatus.PROCESSING;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public CustodyWebhookEventStatus getStatus() { return status; }
    public void setStatus(CustodyWebhookEventStatus status) { this.status = status; }
    public UUID getDepositId() { return depositId; }
    public void setDepositId(UUID depositId) { this.depositId = depositId; }
    public UUID getWithdrawalOperationId() { return withdrawalOperationId; }
    public void setWithdrawalOperationId(UUID withdrawalOperationId) { this.withdrawalOperationId = withdrawalOperationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
