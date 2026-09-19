package com.oakpay.auth.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    private final AuditLogService service;

    public AuditLogController(AuditLogService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<AuditResponse>> recent(@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.recent(limit).stream().map(AuditResponse::from).toList());
    }

    public record AuditResponse(UUID id, UUID actorUserId, String actorType, String action,
                                String resourceType, String resourceId, String outcome,
                                String ipAddress, String metadata, LocalDateTime createdAt) {
        static AuditResponse from(AuditLog l) {
            return new AuditResponse(l.getId(), l.getActorUserId(), l.getActorType(), l.getAction(),
                    l.getResourceType(), l.getResourceId(), l.getOutcome(), l.getIpAddress(),
                    l.getMetadata(), l.getCreatedAt());
        }
    }
}
