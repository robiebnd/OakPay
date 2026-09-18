package com.oakpay.auth.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false, updatable = false) private UUID userId;
    @Column(nullable = false, length = 50, updatable = false) private String type;
    @Column(nullable = false, length = 180, updatable = false) private String title;
    @Column(nullable = false, length = 1000, updatable = false) private String message;
    @Column(columnDefinition = "TEXT", updatable = false) private String data;
    @Column(name = "read_at") private LocalDateTime readAt;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId(){return id;} public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public String getType(){return type;} public void setType(String v){type=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getData(){return data;} public void setData(String v){data=v;}
    public LocalDateTime getReadAt(){return readAt;} public void setReadAt(LocalDateTime v){readAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
