package com.oakpay.wallet.custody;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "custody_operations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_custody_operation_type_key", columnNames = {"operation_type", "idempotency_key"}),
        @UniqueConstraint(name = "uk_custody_operation_provider_ref", columnNames = {"provider_name", "provider_reference"})
})
public class CustodyOperation {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 30, updatable = false)
    private CustodyOperationType operationType;

    @Column(name = "idempotency_key", nullable = false, length = 150, updatable = false)
    private String idempotencyKey;

    @Column(name = "provider_name", nullable = false, length = 80)
    private String providerName;

    @Column(name = "provider_reference", length = 150)
    private String providerReference;

    @Column(name = "provider_address", length = 255)
    private String providerAddress;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 10, updatable = false)
    private String currency;

    @Column(length = 30, updatable = false)
    private String network;

    @Column(precision = 30, scale = 8, updatable = false)
    private BigDecimal amount;

    @Column(name = "destination_address", length = 255, updatable = false)
    private String destinationAddress;

    @Column(name = "memo_tag", length = 100, updatable = false)
    private String memoTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustodyOperationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = CustodyOperationStatus.REQUESTED;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public CustodyOperationType getOperationType() { return operationType; }
    public void setOperationType(CustodyOperationType operationType) { this.operationType = operationType; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public String getProviderAddress() { return providerAddress; }
    public void setProviderAddress(String providerAddress) { this.providerAddress = providerAddress; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    public String getMemoTag() { return memoTag; }
    public void setMemoTag(String memoTag) { this.memoTag = memoTag; }
    public CustodyOperationStatus getStatus() { return status; }
    public void setStatus(CustodyOperationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
