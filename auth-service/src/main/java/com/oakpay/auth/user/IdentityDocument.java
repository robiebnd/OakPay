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
@Table(name = "identity_documents")
public class IdentityDocument {
    @Id @Column(nullable = false, updatable = false) private UUID id;
    @Column(nullable = false) private UUID kycProfileId;
    @Column(nullable = false, length = 40) private String documentType;
    @Column(length = 100) private String documentNumber;
    @Column(length = 500) private String frontDocumentPath;
    @Column(length = 500) private String backDocumentPath;
    @Column(nullable = false, length = 30) private String status = "PENDING";
    @Column(length = 500) private String rejectionReason;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    @PrePersist void prePersist(){if(id==null)id=UUID.randomUUID();LocalDateTime now=LocalDateTime.now();if(createdAt==null)createdAt=now;if(updatedAt==null)updatedAt=now;}
    @PreUpdate void preUpdate(){updatedAt=LocalDateTime.now();}
    public UUID getId(){return id;} public UUID getKycProfileId(){return kycProfileId;} public void setKycProfileId(UUID v){kycProfileId=v;}
    public String getDocumentType(){return documentType;} public void setDocumentType(String v){documentType=v;}
    public String getDocumentNumber(){return documentNumber;} public void setDocumentNumber(String v){documentNumber=v;}
    public String getFrontDocumentPath(){return frontDocumentPath;} public void setFrontDocumentPath(String v){frontDocumentPath=v;}
    public String getBackDocumentPath(){return backDocumentPath;} public void setBackDocumentPath(String v){backDocumentPath=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getRejectionReason(){return rejectionReason;} public void setRejectionReason(String v){rejectionReason=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
