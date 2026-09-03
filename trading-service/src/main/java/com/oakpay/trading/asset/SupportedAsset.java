package com.oakpay.trading.asset;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "supported_assets", indexes = {
        @Index(name = "idx_supported_asset_status", columnList = "status")
})
public class SupportedAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String assetType;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal minTradeAmount;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal minDepositAmount;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal minWithdrawalAmount;

    @Column(nullable = false)
    private Integer decimalPlaces;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    @Column(nullable = false)
    private Boolean p2pEnabled;

    @Column(nullable = false)
    private Boolean spotEnabled;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = AssetStatus.ACTIVE;
        if (p2pEnabled == null) p2pEnabled = true;
        if (spotEnabled == null) spotEnabled = false;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public UUID getId() { return id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String v) { symbol = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String v) { assetType = v; }
    public BigDecimal getMinTradeAmount() { return minTradeAmount; }
    public void setMinTradeAmount(BigDecimal v) { minTradeAmount = v; }
    public BigDecimal getMinDepositAmount() { return minDepositAmount; }
    public void setMinDepositAmount(BigDecimal v) { minDepositAmount = v; }
    public BigDecimal getMinWithdrawalAmount() { return minWithdrawalAmount; }
    public void setMinWithdrawalAmount(BigDecimal v) { minWithdrawalAmount = v; }
    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer v) { decimalPlaces = v; }
    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus v) { status = v; }
    public Boolean getP2pEnabled() { return p2pEnabled; }
    public void setP2pEnabled(Boolean v) { p2pEnabled = v; }
    public Boolean getSpotEnabled() { return spotEnabled; }
    public void setSpotEnabled(Boolean v) { spotEnabled = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
