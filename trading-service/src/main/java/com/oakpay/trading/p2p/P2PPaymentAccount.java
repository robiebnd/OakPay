package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_payment_accounts", indexes = {
        @Index(name = "idx_p2p_payment_accounts_owner", columnList = "owner_id")
})
public class P2PPaymentAccount {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 50)
    private String method;

    @Column(name = "account_name", nullable = false, length = 120)
    private String accountName;

    @Column(name = "account_identifier", nullable = false, length = 150)
    private String accountIdentifier;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID v) { ownerId = v; }
    public String getMethod() { return method; }
    public void setMethod(String v) { method = v; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String v) { accountName = v; }
    public String getAccountIdentifier() { return accountIdentifier; }
    public void setAccountIdentifier(String v) { accountIdentifier = v; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String v) { instructions = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
