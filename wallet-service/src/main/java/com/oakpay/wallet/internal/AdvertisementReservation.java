package com.oakpay.wallet.internal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "advertisement_reservations", indexes = {
        @Index(name = "idx_ad_res_user_currency", columnList = "user_id,currency"),
        @Index(name = "idx_ad_res_status", columnList = "status")
})
public class AdvertisementReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String currency;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal reservedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AdvertisementReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = AdvertisementReservationStatus.RESERVED;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID v) { userId = v; }
    public String getCurrency() { return currency; }
    public void setCurrency(String v) { currency = v; }
    public String getReference() { return reference; }
    public void setReference(String v) { reference = v; }
    public BigDecimal getReservedAmount() { return reservedAmount; }
    public void setReservedAmount(BigDecimal v) { reservedAmount = v; }
    public AdvertisementReservationStatus getStatus() { return status; }
    public void setStatus(AdvertisementReservationStatus v) { status = v; }
}
