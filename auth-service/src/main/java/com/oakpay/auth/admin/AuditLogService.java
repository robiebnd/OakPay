package com.oakpay.auth.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional
    public void record(UUID actorUserId, String actorType, String action, String resourceType,
                       String resourceId, String outcome, String ipAddress, String metadata) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorUserId);
        log.setActorType(normalize(actorType, 20, "USER"));
        log.setAction(normalize(action, 80, "UNKNOWN"));
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setOutcome(normalize(outcome, 20, "SUCCESS"));
        log.setIpAddress(ipAddress);
        log.setMetadata(metadata);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> recent(int limit) {
        int safe = Math.min(Math.max(limit, 1), 200);
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safe));
    }

    private String normalize(String value, int max, String fallback) {
        String v = value == null || value.isBlank() ? fallback : value.trim().toUpperCase();
        return v.length() > max ? v.substring(0, max) : v;
    }
}
