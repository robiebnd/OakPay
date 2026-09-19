package com.oakpay.auth.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/audit-logs")
public class InternalAuditLogController {
    private final AuditLogService auditLogService;
    private final String internalSecret;

    public InternalAuditLogController(
            AuditLogService auditLogService,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.auditLogService = auditLogService;
        this.internalSecret = internalSecret;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void record(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @Valid @RequestBody AuditRequest request) {
        if (suppliedSecret == null || internalSecret == null ||
                !java.security.MessageDigest.isEqual(
                        internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        suppliedSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal service secret");
        }
        auditLogService.record(request.actorUserId(), request.actorType(), request.action(),
                request.resourceType(), request.resourceId(), request.outcome(),
                request.ipAddress(), request.metadata());
    }

    public record AuditRequest(
            UUID actorUserId,
            @NotBlank String actorType,
            @NotBlank String action,
            String resourceType,
            String resourceId,
            @NotBlank String outcome,
            String ipAddress,
            String metadata) {}
}
