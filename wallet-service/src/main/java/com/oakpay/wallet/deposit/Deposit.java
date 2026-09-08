package com.oakpay.wallet.deposit;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deposits", uniqueConstraints = {
        @UniqueConstraint(name = "uk_deposit_tx_network", columnNames = {"tx_hash", "network"})
})
public class Deposit {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "deposit_address_id", nullable = false, updatable = false)
    private UUID depositAddressId;

    @Column(nullable = false, length = 10, updatable = false)
    private String currency;

    @Column(nullable = false, length = 30, updatable = false)
    private String network;

    @Column(nullable = false, length = 255, updatable = false)
    private String address;

    @Column(name = "tx_hash", nullable = false, length = 150, updatable = false)
    private String txHash;

    @Column(nullable = false, precision = 30, scale = 8, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private int confirmations;

    @Column(name = "required_confirmations", nullable = false)
    private int requiredConfirmations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DepositStatus status;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = DepositStatus.PENDING;
        LocalDateTime now = LocalDateTime.now();
        if (detectedAt == null) detectedAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getDepositAddressId() { return depositAddressId; }
    public void setDepositAddressId(UUID depositAddressId) { this.depositAddressId = depositAddressId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency == null ? null : currency.trim().toUpperCase(); }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network == null ? null : network.trim().toUpperCase(); }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address == null ? null : address.trim(); }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash == null ? null : txHash.trim(); }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public int getConfirmations() { return confirmations; }
    public void setConfirmations(int confirmations) { this.confirmations = confirmations; }
    public int getRequiredConfirmations() { return requiredConfirmations; }
    public void setRequiredConfirmations(int requiredConfirmations) { this.requiredConfirmations = requiredConfirmations; }
    public DepositStatus getStatus() { return status; }
    public void setStatus(DepositStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
