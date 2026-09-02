package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_trades", indexes = {
        @Index(name = "idx_p2p_buyer", columnList = "buyer_id"),
        @Index(name = "idx_p2p_seller", columnList = "seller_id"),
        @Index(name = "idx_p2p_status", columnList = "status")
})
public class P2PTrade {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "asset", nullable = false, length = 10, updatable = false)
    private String asset;

    @Column(name = "fiat_currency", nullable = false, length = 10, updatable = false)
    private String fiatCurrency;

    @Column(nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal unitPrice;

    @Column(name = "fiat_amount", nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal fiatAmount;

    @Column(name = "payment_method", nullable = false, length = 50, updatable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private P2PTradeStatus status;

    @Column(name = "payment_reference", length = 150)
    private String paymentReference;

    @Column(name = "payment_note", columnDefinition = "TEXT")
    private String paymentNote;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = P2PTradeStatus.ESCROWED;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getBuyerId() { return buyerId; }
    public void setBuyerId(UUID v) { buyerId = v; }
    public UUID getSellerId() { return sellerId; }
    public void setSellerId(UUID v) { sellerId = v; }
    public String getAsset() { return asset; }
    public void setAsset(String v) { asset = v; }
    public String getFiatCurrency() { return fiatCurrency; }
    public void setFiatCurrency(String v) { fiatCurrency = v; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal v) { quantity = v; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal v) { unitPrice = v; }
    public BigDecimal getFiatAmount() { return fiatAmount; }
    public void setFiatAmount(BigDecimal v) { fiatAmount = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { paymentMethod = v; }
    public P2PTradeStatus getStatus() { return status; }
    public void setStatus(P2PTradeStatus v) { status = v; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String v) { paymentReference = v; }
    public String getPaymentNote() { return paymentNote; }
    public void setPaymentNote(String v) { paymentNote = v; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime v) { expiresAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
