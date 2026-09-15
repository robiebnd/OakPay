package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p")
public class P2PDisputeController {

    private final P2PDisputeService service;
    private final String adminSecret;

    public P2PDisputeController(
            P2PDisputeService service,
            @Value("${oakpay.admin.dispute-secret}") String adminSecret) {
        this.service = service;
        this.adminSecret = adminSecret;
    }

    @PostMapping("/trades/{tradeId}/dispute")
    public ResponseEntity<P2PDisputeDtos.DisputeResponse> open(
            @PathVariable UUID tradeId,
            @RequestBody P2PDisputeDtos.OpenRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.open(userId(authentication), tradeId, request));
    }

    @GetMapping("/disputes/mine")
    public List<P2PDisputeDtos.DisputeResponse> mine(Authentication authentication) {
        return service.mine(userId(authentication));
    }

    @GetMapping("/admin/disputes")
    public List<P2PDisputeDtos.DisputeResponse> adminDisputes(
            @RequestHeader(value = "X-OakPay-Admin-Secret", required = false) String suppliedSecret,
            Authentication authentication) {
        requireAdmin(authentication, suppliedSecret);
        return service.openDisputes();
    }

    @GetMapping("/admin/disputes/{disputeId}/audit")
    public List<P2PDisputeDtos.AuditResponse> audit(
            @PathVariable UUID disputeId,
            @RequestHeader(value = "X-OakPay-Admin-Secret", required = false) String suppliedSecret,
            Authentication authentication) {
        requireAdmin(authentication, suppliedSecret);
        return service.audit(disputeId);
    }

    @PostMapping("/admin/disputes/{disputeId}/resolve")
    public P2PDisputeDtos.DisputeResponse resolve(
            @PathVariable UUID disputeId,
            @RequestBody P2PDisputeDtos.ResolveRequest request,
            @RequestHeader(value = "X-OakPay-Admin-Secret", required = false) String suppliedSecret,
            Authentication authentication) {
        requireAdmin(authentication, suppliedSecret);
        return service.resolve(userId(authentication), disputeId, request);
    }

    private UUID userId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid authenticated user id");
        }
    }

    /**
     * Trading service validates the shared admin secret for admin-only operations.
     * The auth service proxy is the public admin entry point and already enforces
     * the ADMIN role before forwarding the request.
     */
    private void requireAdmin(Authentication authentication, String suppliedSecret) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        if (suppliedSecret == null || adminSecret == null ||
                !MessageDigest.isEqual(
                        suppliedSecret.getBytes(StandardCharsets.UTF_8),
                        adminSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Administrator access required");
        }
    }
}
