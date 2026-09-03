package com.oakpay.trading.p2p;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_commissions", uniqueConstraints = @UniqueConstraint(name = "uk_p2p_commission_trade", columnNames = "trade_id"))
public class P2PCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trade_id", nullable = false, updatable = false)
    private UUID tradeId;

    @Column(name = "payer_id", nullable = false, updatable = false)
    private UUID payerId;

    @Column(name = "fiat_currency", nullable = false, length = 10, updatable = false)
    private String fiatCurrency;

    @Column(name = "fiat_amount", nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal fiatAmount;

    @Column(name = "rate", nullable = false, precision = 10, scale = 8, updatable = false)
    private BigDecimal rate;

    @Column(name = "commission_amount", nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private P2PCommissionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = P2PCommissionStatus.ASSESSED;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID value) { tradeId = value; }
    public UUID getPayerId() { return payerId; }
    public void setPayerId(UUID value) { payerId = value; }
    public String getFiatCurrency() { return fiatCurrency; }
    public void setFiatCurrency(String value) { fiatCurrency = value; }
    public BigDecimal getFiatAmount() { return fiatAmount; }
    public void setFiatAmount(BigDecimal value) { fiatAmount = value; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal value) { rate = value; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal value) { commissionAmount = value; }
    public P2PCommissionStatus getStatus() { return status; }
    public void setStatus(P2PCommissionStatus value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
