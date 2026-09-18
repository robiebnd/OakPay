package com.oakpay.auth.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public final class NotificationDtos {
    private NotificationDtos() {}
    public record NotificationResponse(UUID id, String type, String title, String message, String data, boolean read, LocalDateTime createdAt) {}
    public record DeviceRequest(@NotBlank String expoPushToken, @NotBlank @Size(max=20) String platform, @Size(max=255) String deviceId) {}
    public record InternalCreateRequest(UUID userId, @NotBlank @Size(max=50) String type, @NotBlank @Size(max=180) String title, @NotBlank @Size(max=1000) String message, String data) {}
}
