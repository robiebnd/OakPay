package com.oakpay.trading.p2p;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="platform_fee_settings")
public class PlatformFeeSetting {
    @Id
    @Column(name="setting_key", length=50, nullable=false, updatable=false)
    private String settingKey;

    @Column(name="setting_value", nullable=false, precision=20, scale=10)
    private BigDecimal settingValue;

    @Column(name="updated_by")
    private java.util.UUID updatedBy;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void timestamps() { updatedAt = LocalDateTime.now(); }

    public String getSettingKey(){return settingKey;}
    public void setSettingKey(String v){settingKey=v;}
    public BigDecimal getSettingValue(){return settingValue;}
    public void setSettingValue(BigDecimal v){settingValue=v;}
    public java.util.UUID getUpdatedBy(){return updatedBy;}
    public void setUpdatedBy(java.util.UUID v){updatedBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
}
