package com.oakpay.trading.market;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trading_pairs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trading_pair_symbol", columnNames = "symbol")
}, indexes = {
        @Index(name = "idx_trading_pair_status", columnList = "status"),
        @Index(name = "idx_trading_pair_currencies", columnList = "base_currency,quote_currency")
})
public class TradingPair {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30, updatable = false)
    private String symbol;

    @Column(name = "base_currency", nullable = false, length = 20, updatable = false)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 20, updatable = false)
    private String quoteCurrency;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal minPrice;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal maxPrice;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal priceTickSize;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal minQuantity;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal quantityStepSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradingPairStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = TradingPairStatus.ACTIVE;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String value) { symbol = value; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String value) { baseCurrency = value; }
    public String getQuoteCurrency() { return quoteCurrency; }
    public void setQuoteCurrency(String value) { quoteCurrency = value; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal value) { minPrice = value; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal value) { maxPrice = value; }
    public BigDecimal getPriceTickSize() { return priceTickSize; }
    public void setPriceTickSize(BigDecimal value) { priceTickSize = value; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal value) { minQuantity = value; }
    public BigDecimal getQuantityStepSize() { return quantityStepSize; }
    public void setQuantityStepSize(BigDecimal value) { quantityStepSize = value; }
    public TradingPairStatus getStatus() { return status; }
    public void setStatus(TradingPairStatus value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
