package com.oakpay.auth.notification;

import com.oakpay.auth.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // Development-only endpoint used by the mobile notification test button.
    // It is still authenticated and creates a real database notification for the current user.
    @PostMapping("/test")
    public ResponseEntity<NotificationDtos.NotificationResponse> test(
            @AuthenticationPrincipal UserPrincipal p) {
        if (p == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.createTest(p.getUserId()));
    }

    @GetMapping
    public List<NotificationDtos.NotificationResponse> list(
            @AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue = "50") int limit) {
        return service.list(p.getUserId(), limit);
    }

    @GetMapping("/unread-count")
    public long unread(@AuthenticationPrincipal UserPrincipal p) {
        return service.unreadCount(p.getUserId());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal UserPrincipal p,
            @PathVariable UUID id) {
        service.markRead(p.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> readPost(
            @AuthenticationPrincipal UserPrincipal p,
            @PathVariable UUID id) {
        service.markRead(p.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal UserPrincipal p) {
        service.markAllRead(p.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/devices")
    public ResponseEntity<Void> device(
            @AuthenticationPrincipal UserPrincipal p,
            @Valid @RequestBody NotificationDtos.DeviceRequest r) {
        service.registerDevice(p.getUserId(), r);
        return ResponseEntity.noContent().build();
    }
}
