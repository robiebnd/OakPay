package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_advertisements", indexes = {
        @Index(name = "idx_p2p_ads_search", columnList = "side,asset,fiat_currency,status"),
        @Index(name = "idx_p2p_ads_owner", columnList = "owner_id")
})
public class Advertisement {
    @Id @Column(nullable = false, updatable = false) private UUID id;
    @Column(name = "owner_id", nullable = false, updatable = false) private UUID ownerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10, updatable = false) private OrderSide side;
    @Column(nullable = false, length = 10, updatable = false) private String asset;
    @Column(name = "fiat_currency", nullable = false, length = 10, updatable = false) private String fiatCurrency;
    @Column(nullable = false, precision = 20, scale = 2, updatable = false) private BigDecimal price;
    @Column(name = "total_quantity", nullable = false, precision = 20, scale = 2) private BigDecimal totalQuantity;
    @Column(name = "available_quantity", nullable = false, precision = 20, scale = 2) private BigDecimal availableQuantity;
    @Column(name = "min_quantity", nullable = false, precision = 20, scale = 2, updatable = false) private BigDecimal minQuantity;
    @Column(name = "max_quantity", nullable = false, precision = 20, scale = 2, updatable = false) private BigDecimal maxQuantity;
    @Column(name = "payment_methods", nullable = false, length = 500) private String paymentMethods;
    @Column(columnDefinition = "TEXT") private String terms;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 15) private AdStatus status;
    @Column(name = "auto_closed", nullable = false) private boolean autoClosed;
    @Column(name = "reservation_reference", length = 100, unique = true) private String reservationReference;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID(); if (availableQuantity == null) availableQuantity = totalQuantity; if (status == null) status = AdStatus.ACTIVE; LocalDateTime now = LocalDateTime.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; normalize(); }
    @PreUpdate void preUpdate() { normalize(); updatedAt = LocalDateTime.now(); }
    private BigDecimal money(BigDecimal v) { return v == null ? null : v.setScale(2, RoundingMode.HALF_UP); }
    private void normalize() { price = money(price); totalQuantity = money(totalQuantity); availableQuantity = money(availableQuantity); minQuantity = money(minQuantity); maxQuantity = money(maxQuantity); }
    public UUID getId(){return id;} public UUID getOwnerId(){return ownerId;} public void setOwnerId(UUID v){ownerId=v;} public OrderSide getSide(){return side;} public void setSide(OrderSide v){side=v;} public String getAsset(){return asset;} public void setAsset(String v){asset=v;} public String getFiatCurrency(){return fiatCurrency;} public void setFiatCurrency(String v){fiatCurrency=v;} public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=money(v);} public BigDecimal getTotalQuantity(){return totalQuantity;} public void setTotalQuantity(BigDecimal v){totalQuantity=money(v);} public BigDecimal getAvailableQuantity(){return availableQuantity;} public void setAvailableQuantity(BigDecimal v){availableQuantity=money(v);} public BigDecimal getMinQuantity(){return minQuantity;} public void setMinQuantity(BigDecimal v){minQuantity=money(v);} public BigDecimal getMaxQuantity(){return maxQuantity;} public void setMaxQuantity(BigDecimal v){maxQuantity=money(v);} public String getPaymentMethods(){return paymentMethods;} public void setPaymentMethods(String v){paymentMethods=v;} public String getTerms(){return terms;} public void setTerms(String v){terms=v;} public AdStatus getStatus(){return status;} public void setStatus(AdStatus v){status=v;} public boolean isAutoClosed(){return autoClosed;} public void setAutoClosed(boolean v){autoClosed=v;} public String getReservationReference(){return reservationReference;} public void setReservationReference(String v){reservationReference=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
