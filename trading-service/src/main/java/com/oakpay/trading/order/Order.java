package com.oakpay.trading.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_pair_status", columnList = "base_currency,quote_currency,status"),
        @Index(name = "idx_orders_user_id", columnList = "user_id")
})
public class Order {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "base_currency", nullable = false, length = 10, updatable = false)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 10, updatable = false)
    private String quoteCurrency;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal price;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal quantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 38, scale = 18)
    private BigDecimal remainingQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (remainingQuantity == null) remainingQuantity = quantity;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = OrderStatus.OPEN;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public String getQuoteCurrency() { return quoteCurrency; }
    public void setQuoteCurrency(String quoteCurrency) { this.quoteCurrency = quoteCurrency; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
