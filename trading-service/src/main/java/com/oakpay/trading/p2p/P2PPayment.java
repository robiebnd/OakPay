package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_payments", indexes = {
        @Index(name = "idx_p2p_payments_trade", columnList = "trade_id"),
        @Index(name = "idx_p2p_payments_payer", columnList = "payer_id"),
        @Index(name = "idx_p2p_payments_status", columnList = "status")
})
public class P2PPayment {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "trade_id", nullable = false, updatable = false, unique = true)
    private UUID tradeId;

    @Column(name = "payer_id", nullable = false, updatable = false)
    private UUID payerId;

    @Column(name = "payee_id", nullable = false, updatable = false)
    private UUID payeeId;

    @Column(nullable = false, precision = 38, scale = 18, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 10, updatable = false)
    private String currency;

    @Column(name = "payment_method", nullable = false, length = 50, updatable = false)
    private String paymentMethod;

    @Column(name = "payment_reference", nullable = false, length = 150)
    private String paymentReference;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = PaymentStatus.PENDING;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID v) { tradeId = v; }
    public UUID getPayerId() { return payerId; }
    public void setPayerId(UUID v) { payerId = v; }
    public UUID getPayeeId() { return payeeId; }
    public void setPayeeId(UUID v) { payeeId = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { amount = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String v) { currency = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { paymentMethod = v; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String v) { paymentReference = v; }
    public String getNote() { return note; }
    public void setNote(String v) { note = v; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus v) { status = v; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime v) { submittedAt = v; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime v) { verifiedAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
