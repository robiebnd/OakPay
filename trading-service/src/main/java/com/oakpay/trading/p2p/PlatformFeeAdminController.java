package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/admin/platform-settings")
public class PlatformFeeAdminController {
    private final PlatformFeeService service;
    private final String adminSecret;

    public PlatformFeeAdminController(
            PlatformFeeService service,
            @Value("${oakpay.admin.dispute-secret}") String adminSecret) {
        this.service = service;
        this.adminSecret = adminSecret;
    }

    @GetMapping("/fees")
    public Map<String, java.math.BigDecimal> fees(
            @RequestHeader(value="X-OakPay-Admin-Secret", required=false) String supplied) {
        requireAdmin(supplied);
        return service.current();
    }

    @PatchMapping("/fees/{key}")
    public Map<String, java.math.BigDecimal> update(
            @PathVariable String key,
            @RequestParam java.math.BigDecimal value,
            @RequestHeader(value="X-OakPay-Admin-Secret", required=false) String supplied,
            @RequestHeader(value="X-OakPay-Admin-Actor", required=false) String actor) {
        requireAdmin(supplied);
        UUID actorId = null;
        if (actor != null && !actor.isBlank()) {
            try { actorId = UUID.fromString(actor.trim()); } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid admin actor");
            }
        }
        java.math.BigDecimal updated = service.set(key.trim().toUpperCase(), value, actorId);
        return Map.of(key.trim().toUpperCase(), updated);
    }

    private void requireAdmin(String supplied) {
        if (adminSecret == null || adminSecret.isBlank() || supplied == null ||
                !MessageDigest.isEqual(adminSecret.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin credential");
        }
    }
}
