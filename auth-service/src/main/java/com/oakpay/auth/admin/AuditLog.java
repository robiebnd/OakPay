package com.oakpay.auth.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="audit_logs", indexes={
        @Index(name="idx_audit_logs_created_at", columnList="created_at"),
        @Index(name="idx_audit_logs_actor", columnList="actor_user_id,created_at"),
        @Index(name="idx_audit_logs_action", columnList="action,created_at")
})
public class AuditLog {
    @Id
    @Column(nullable=false, updatable=false)
    private UUID id;

    @Column(name="actor_user_id")
    private UUID actorUserId;

    @Column(name="actor_type", nullable=false, length=20)
    private String actorType;

    @Column(nullable=false, length=80)
    private String action;

    @Column(name="resource_type", length=50)
    private String resourceType;

    @Column(name="resource_id", length=100)
    private String resourceId;

    @Column(nullable=false, length=20)
    private String outcome;

    @Column(name="ip_address", length=64)
    private String ipAddress;

    @Column(columnDefinition="TEXT")
    private String metadata;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId(){return id;}
    public UUID getActorUserId(){return actorUserId;}
    public void setActorUserId(UUID v){actorUserId=v;}
    public String getActorType(){return actorType;}
    public void setActorType(String v){actorType=v;}
    public String getAction(){return action;}
    public void setAction(String v){action=v;}
    public String getResourceType(){return resourceType;}
    public void setResourceType(String v){resourceType=v;}
    public String getResourceId(){return resourceId;}
    public void setResourceId(String v){resourceId=v;}
    public String getOutcome(){return outcome;}
    public void setOutcome(String v){outcome=v;}
    public String getIpAddress(){return ipAddress;}
    public void setIpAddress(String v){ipAddress=v;}
    public String getMetadata(){return metadata;}
    public void setMetadata(String v){metadata=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
