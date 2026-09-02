package com.oakpay.trading.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trades_buyer", columnList = "buyer_id"),
        @Index(name = "idx_trades_seller", columnList = "seller_id")
})
public class Trade {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "buy_order_id", nullable = false, updatable = false)
    private UUID buyOrderId;

    @Column(name = "sell_order_id", nullable = false, updatable = false)
    private UUID sellOrderId;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "base_currency", nullable = false, length = 10, updatable = false)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 10, updatable = false)
    private String quoteCurrency;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal price;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal grossValue;

    @Column(name = "buyer_fee", nullable = false, precision = 38, scale = 18)
    private BigDecimal buyerFee;

    @Column(name = "seller_fee", nullable = false, precision = 38, scale = 18)
    private BigDecimal sellerFee;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(UUID v) { buyOrderId = v; }
    public UUID getSellOrderId() { return sellOrderId; }
    public void setSellOrderId(UUID v) { sellOrderId = v; }
    public UUID getBuyerId() { return buyerId; }
    public void setBuyerId(UUID v) { buyerId = v; }
    public UUID getSellerId() { return sellerId; }
    public void setSellerId(UUID v) { sellerId = v; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String v) { baseCurrency = v; }
    public String getQuoteCurrency() { return quoteCurrency; }
    public void setQuoteCurrency(String v) { quoteCurrency = v; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal v) { price = v; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal v) { quantity = v; }
    public BigDecimal getGrossValue() { return grossValue; }
    public void setGrossValue(BigDecimal v) { grossValue = v; }
    public BigDecimal getBuyerFee() { return buyerFee; }
    public void setBuyerFee(BigDecimal v) { buyerFee = v; }
    public BigDecimal getSellerFee() { return sellerFee; }
    public void setSellerFee(BigDecimal v) { sellerFee = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
