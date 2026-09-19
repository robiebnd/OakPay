package com.oakpay.wallet.internal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="wallet_operations", uniqueConstraints=@UniqueConstraint(name="uk_wallet_operation_type_reference", columnNames={"operation_type","reference"}))
public class WalletOperation {
    @Id
    @Column(nullable=false, updatable=false)
    private UUID id;
    @Column(name="operation_type", nullable=false, length=30, updatable=false)
    private String operationType;
    @Column(nullable=false, length=100, updatable=false)
    private String reference;
    @Column(name="request_hash", nullable=false, length=64, updatable=false)
    private String requestHash;
    @Column(name="user_id", nullable=false, updatable=false)
    private UUID userId;
    @Column(nullable=false, length=10, updatable=false)
    private String currency;
    @Column(nullable=false, precision=30, scale=8, updatable=false)
    private BigDecimal amount;
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        if(id==null) id=UUID.randomUUID();
        if(createdAt==null) createdAt=LocalDateTime.now();
    }
    public UUID getId(){return id;}
    public String getOperationType(){return operationType;}
    public void setOperationType(String v){operationType=v;}
    public String getReference(){return reference;}
    public void setReference(String v){reference=v;}
    public String getRequestHash(){return requestHash;}
    public void setRequestHash(String v){requestHash=v;}
    public UUID getUserId(){return userId;}
    public void setUserId(UUID v){userId=v;}
    public String getCurrency(){return currency;}
    public void setCurrency(String v){currency=v;}
    public BigDecimal getAmount(){return amount;}
    public void setAmount(BigDecimal v){amount=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
