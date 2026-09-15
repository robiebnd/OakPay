package com.oakpay.trading.p2p;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p")
public class P2PDisputeController {

    private final P2PDisputeService service;

    public P2PDisputeController(P2PDisputeService service) {
        this.service = service;
    }

    /*
     * ============================================================
     * CLIENT DISPUTES
     * ============================================================
     */

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
    public List<P2PDisputeDtos.DisputeResponse> mine(
            Authentication authentication) {

        return service.mine(userId(authentication));
    }

    /*
     * ============================================================
     * ADMIN DISPUTES
     * ============================================================
     */

    @GetMapping("/admin/disputes")
    public List<P2PDisputeDtos.DisputeResponse> adminDisputes(
            Authentication authentication) {

        requireAdmin(authentication);

        return service.openDisputes();
    }

    @GetMapping("/admin/disputes/{disputeId}/audit")
    public List<P2PDisputeDtos.AuditResponse> audit(
            @PathVariable UUID disputeId,
            Authentication authentication) {

        requireAdmin(authentication);

        return service.audit(disputeId);
    }

    @PostMapping("/admin/disputes/{disputeId}/resolve")
    public P2PDisputeDtos.DisputeResponse resolve(
            @PathVariable UUID disputeId,
            @RequestBody P2PDisputeDtos.ResolveRequest request,
            Authentication authentication) {

        requireAdmin(authentication);

        return service.resolve(
                userId(authentication),
                disputeId,
                request
        );
    }

    /*
     * ============================================================
     * AUTHORIZATION
     * ============================================================
     */

    private UUID userId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }

        return UUID.fromString(authentication.getName());
    }

    private void requireAdmin(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                                || "ADMIN".equals(authority.getAuthority())
                );

        if (!admin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator access required"
            );
        }
    }
}