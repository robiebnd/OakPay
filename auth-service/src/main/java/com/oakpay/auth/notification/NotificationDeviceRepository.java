package com.oakpay.auth.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDeviceRepository extends JpaRepository<NotificationDevice, UUID> {
    List<NotificationDevice> findAllByUserIdAndActiveTrue(UUID userId);
    Optional<NotificationDevice> findByExpoPushToken(String token);
}
