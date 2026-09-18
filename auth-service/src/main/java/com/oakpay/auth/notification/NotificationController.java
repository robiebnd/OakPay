package com.oakpay.auth.notification;

import com.oakpay.auth.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;
    private final boolean testEnabled;

    public NotificationController(
            NotificationService service,
            @Value("${oakpay.notifications.test-enabled:false}") boolean testEnabled) {
        this.service = service;
        this.testEnabled = testEnabled;
    }
    @PostMapping("/test")
    public ResponseEntity<NotificationDtos.NotificationResponse> test(
            @AuthenticationPrincipal UserPrincipal p) {
        if (!testEnabled) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(service.createTest(p.getUserId()));
    }

    @GetMapping public List<NotificationDtos.NotificationResponse> list(@AuthenticationPrincipal UserPrincipal p,@RequestParam(defaultValue="50") int limit){return service.list(p.getUserId(),limit);}
    @GetMapping("/unread-count") public long unread(@AuthenticationPrincipal UserPrincipal p){return service.unreadCount(p.getUserId());}
    @PatchMapping("/{id}/read") public ResponseEntity<Void> read(@AuthenticationPrincipal UserPrincipal p,@PathVariable UUID id){service.markRead(p.getUserId(),id);return ResponseEntity.noContent().build();}
    @PostMapping("/{id}/read") public ResponseEntity<Void> readPost(@AuthenticationPrincipal UserPrincipal p,@PathVariable UUID id){service.markRead(p.getUserId(),id);return ResponseEntity.noContent().build();}
    @PostMapping("/read-all") public ResponseEntity<Void> readAll(@AuthenticationPrincipal UserPrincipal p){service.markAllRead(p.getUserId());return ResponseEntity.noContent().build();}
    @PostMapping("/devices") public ResponseEntity<Void> device(@AuthenticationPrincipal UserPrincipal p,@Valid @RequestBody NotificationDtos.DeviceRequest r){service.registerDevice(p.getUserId(),r);return ResponseEntity.noContent().build();}
}
