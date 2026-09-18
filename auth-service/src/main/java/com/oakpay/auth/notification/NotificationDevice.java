package com.oakpay.auth.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_devices")
public class NotificationDevice {
    @Id @Column(nullable = false, updatable = false) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "expo_push_token", nullable = false, unique = true, length = 255) private String expoPushToken;
    @Column(nullable = false, length = 20) private String platform;
    @Column(name = "device_id", length = 255) private String deviceId;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    @PrePersist void prePersist(){ if(id==null)id=UUID.randomUUID(); LocalDateTime n=LocalDateTime.now(); if(createdAt==null)createdAt=n; if(updatedAt==null)updatedAt=n; }
    @PreUpdate void preUpdate(){updatedAt=LocalDateTime.now();}
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public String getExpoPushToken(){return expoPushToken;} public void setExpoPushToken(String v){expoPushToken=v;}
    public String getPlatform(){return platform;} public void setPlatform(String v){platform=v;}
    public String getDeviceId(){return deviceId;} public void setDeviceId(String v){deviceId=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
