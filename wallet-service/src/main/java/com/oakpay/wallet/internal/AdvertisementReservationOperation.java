package com.oakpay.wallet.internal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "advertisement_reservation_operations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ad_res_operation", columnNames = {"reservation_reference", "operation_reference"})
})
public class AdvertisementReservationOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "reservation_reference", nullable = false, length = 100)
    private String reservationReference;
    @Column(name = "operation_reference", nullable = false, length = 100)
    private String operationReference;
    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal amount;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public UUID getId() { return id; }
    public String getReservationReference() { return reservationReference; }
    public void setReservationReference(String v) { reservationReference = v; }
    public String getOperationReference() { return operationReference; }
    public void setOperationReference(String v) { operationReference = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { amount = v; }
}
