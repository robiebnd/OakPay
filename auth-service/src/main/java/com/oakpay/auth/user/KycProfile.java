package com.oakpay.auth.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_profiles")
public class KycProfile {
    @Id @Column(nullable = false, updatable = false) private UUID id;
    @Column(nullable = false, unique = true) private UUID userId;
    @Column(nullable = false, length = 30) private String status = "NOT_STARTED";
    @Column(length = 500) private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID(); LocalDateTime now = LocalDateTime.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public void setUserId(UUID userId){this.userId=userId;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    public String getRejectionReason(){return rejectionReason;} public void setRejectionReason(String rejectionReason){this.rejectionReason=rejectionReason;}
    public LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(LocalDateTime submittedAt){this.submittedAt=submittedAt;}
    public LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(LocalDateTime reviewedAt){this.reviewedAt=reviewedAt;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
