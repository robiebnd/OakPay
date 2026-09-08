package com.oakpay.wallet.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {
    private static final Set<String> FIAT_CURRENCIES = Set.of("USD", "ZWG");
    private static final int FIAT_SCALE = 2;
    private static final int CRYPTO_SCALE = 8;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "available_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal availableBalance;

    @Column(name = "locked_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal lockedBalance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (availableBalance == null) availableBalance = BigDecimal.ZERO.setScale(scaleForCurrency());
        if (lockedBalance == null) lockedBalance = BigDecimal.ZERO.setScale(scaleForCurrency());
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        availableBalance = money(availableBalance);
        lockedBalance = money(lockedBalance);
        updatedAt = LocalDateTime.now();
    }

    private int scaleForCurrency() {
        return currency != null && FIAT_CURRENCIES.contains(currency.toUpperCase()) ? FIAT_SCALE : CRYPTO_SCALE;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(scaleForCurrency(), RoundingMode.HALF_UP);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = money(availableBalance); }
    public BigDecimal getLockedBalance() { return lockedBalance; }
    public void setLockedBalance(BigDecimal lockedBalance) { this.lockedBalance = money(lockedBalance); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
