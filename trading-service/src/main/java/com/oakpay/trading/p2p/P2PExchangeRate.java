package com.oakpay.trading.p2p;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_exchange_rates")
public class P2PExchangeRate {
    @Id
    private UUID id;

    @Column(name = "base_currency", nullable = false, length = 10)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 10)
    private String quoteCurrency;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public String getBaseCurrency() { return baseCurrency; }
    public String getQuoteCurrency() { return quoteCurrency; }
    public BigDecimal getRate() { return rate; }
    public String getSource() { return source; }
    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
