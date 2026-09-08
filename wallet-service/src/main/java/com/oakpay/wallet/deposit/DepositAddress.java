package com.oakpay.wallet.deposit;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deposit_addresses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_deposit_address_user_asset_network", columnNames = {"user_id", "currency", "network"}),
        @UniqueConstraint(name = "uk_deposit_address_address_network", columnNames = {"address", "network"})
})
public class DepositAddress {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 10, updatable = false)
    private String currency;

    @Column(nullable = false, length = 30, updatable = false)
    private String network;

    @Column(nullable = false, length = 255, updatable = false)
    private String address;

    @Column(length = 100, updatable = false)
    private String memoTag;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DepositAddressStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = DepositAddressStatus.ACTIVE;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency == null ? null : currency.trim().toUpperCase(); }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network == null ? null : network.trim().toUpperCase(); }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address == null ? null : address.trim(); }
    public String getMemoTag() { return memoTag; }
    public void setMemoTag(String memoTag) { this.memoTag = memoTag == null || memoTag.isBlank() ? null : memoTag.trim(); }
    public DepositAddressStatus getStatus() { return status; }
    public void setStatus(DepositAddressStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
