package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_advertisements", indexes = {
        @Index(name = "idx_p2p_ads_search", columnList = "side,asset,fiat_currency,status"),
        @Index(name = "idx_p2p_ads_owner", columnList = "owner_id")
})
public class Advertisement {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private OrderSide side;

    @Column(nullable = false, length = 10, updatable = false)
    private String asset;

    @Column(name = "fiat_currency", nullable = false, length = 10, updatable = false)
    private String fiatCurrency;

    @Column(nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal price;

    @Column(name = "total_quantity", nullable = false, precision = 38, scale = 18)
    private BigDecimal totalQuantity;

    @Column(name = "available_quantity", nullable = false, precision = 38, scale = 18)
    private BigDecimal availableQuantity;

    @Column(name = "min_quantity", nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal minQuantity;

    @Column(name = "max_quantity", nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal maxQuantity;

    @Column(name = "payment_methods", nullable = false, length = 500)
    private String paymentMethods;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AdStatus status;

    @Column(name = "auto_closed", nullable = false)
    private boolean autoClosed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (availableQuantity == null) availableQuantity = totalQuantity;
        if (status == null) status = AdStatus.ACTIVE;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID v) { ownerId = v; }
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide v) { side = v; }
    public String getAsset() { return asset; }
    public void setAsset(String v) { asset = v; }
    public String getFiatCurrency() { return fiatCurrency; }
    public void setFiatCurrency(String v) { fiatCurrency = v; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal v) { price = v; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal v) { totalQuantity = v; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal v) { availableQuantity = v; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal v) { minQuantity = v; }
    public BigDecimal getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(BigDecimal v) { maxQuantity = v; }
    public String getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(String v) { paymentMethods = v; }
    public String getTerms() { return terms; }
    public void setTerms(String v) { terms = v; }
    public AdStatus getStatus() { return status; }
    public void setStatus(AdStatus v) { status = v; }
    public boolean isAutoClosed() { return autoClosed; }
    public void setAutoClosed(boolean v) { autoClosed = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
