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
    private static final String ADMIN_HEADER = "X-OakPay-Admin-Secret";
    private final P2PDisputeService service;

    public P2PDisputeController(P2PDisputeService service) { this.service = service; }

    @PostMapping("/trades/{tradeId}/dispute")
    public ResponseEntity<P2PDisputeDtos.DisputeResponse> open(
            @PathVariable UUID tradeId,
            @RequestBody P2PDisputeDtos.OpenRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.open(userId(authentication), tradeId, request));
    }

    @GetMapping("/disputes/mine")
    public List<P2PDisputeDtos.DisputeResponse> mine(Authentication authentication) {
        return service.mine(userId(authentication));
    }

    @GetMapping("/admin/disputes")
    public List<P2PDisputeDtos.DisputeResponse> open(@RequestHeader(value = ADMIN_HEADER, required = false) String secret) {
        return service.openDisputes(secret);
    }

    @GetMapping("/admin/disputes/{disputeId}/audit")
    public List<P2PDisputeDtos.AuditResponse> audit(
            @PathVariable UUID disputeId,
            @RequestHeader(value = ADMIN_HEADER, required = false) String secret) {
        return service.audit(disputeId, secret);
    }

    @PostMapping("/admin/disputes/{disputeId}/resolve")
    public P2PDisputeDtos.DisputeResponse resolve(
            @PathVariable UUID disputeId,
            @RequestBody P2PDisputeDtos.ResolveRequest request,
            @RequestHeader(value = ADMIN_HEADER, required = false) String secret,
            Authentication authentication) {
        return service.resolve(userId(authentication), disputeId, request, secret);
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
