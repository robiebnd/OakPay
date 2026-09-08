package com.oakpay.wallet.ledger;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries", uniqueConstraints = @UniqueConstraint(name = "uk_ledger_reference", columnNames = "reference"))
public class LedgerEntry {
    private static final Set<String> FIAT_CURRENCIES = Set.of("USD", "ZWG");
    private static final int FIAT_SCALE = 2;
    private static final int CRYPTO_SCALE = 8;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30, updatable = false)
    private LedgerTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private LedgerDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false, length = 10, updatable = false)
    private LedgerBalanceType balanceType;

    @Column(nullable = false, length = 10, updatable = false)
    private String currency;

    @Column(nullable = false, precision = 30, scale = 8, updatable = false)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 30, scale = 8, updatable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 30, scale = 8, updatable = false)
    private BigDecimal balanceAfter;

    @Column(nullable = false, unique = true, length = 100, updatable = false)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (direction == null) direction = LedgerDirection.CREDIT;
        if (balanceType == null) balanceType = LedgerBalanceType.AVAILABLE;
        amount = money(amount);
        balanceBefore = money(balanceBefore);
        balanceAfter = money(balanceAfter);
    }

    private int scaleForCurrency() {
        return currency != null && FIAT_CURRENCIES.contains(currency.toUpperCase()) ? FIAT_SCALE : CRYPTO_SCALE;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(scaleForCurrency(), RoundingMode.HALF_UP);
    }

    public UUID getId() { return id; }
    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public LedgerTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(LedgerTransactionType transactionType) { this.transactionType = transactionType; }
    public LedgerStatus getStatus() { return status; }
    public void setStatus(LedgerStatus status) { this.status = status; }
    public LedgerDirection getDirection() { return direction; }
    public void setDirection(LedgerDirection direction) { this.direction = direction; }
    public LedgerBalanceType getBalanceType() { return balanceType; }
    public void setBalanceType(LedgerBalanceType balanceType) { this.balanceType = balanceType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = money(amount); }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = money(balanceBefore); }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = money(balanceAfter); }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
