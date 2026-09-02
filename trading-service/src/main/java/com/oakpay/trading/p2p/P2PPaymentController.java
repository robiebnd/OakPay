package com.oakpay.trading.p2p;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/trades")
public class P2PPaymentController {
    private final P2PPaymentService service;

    public P2PPaymentController(P2PPaymentService service) { this.service = service; }

    @PostMapping("/{tradeId}/payment")
    public ResponseEntity<P2PPaymentDtos.PaymentResponse> submit(
            @PathVariable UUID tradeId,
            @RequestBody P2PPaymentDtos.SubmitRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(service.submit(userId(authentication), tradeId, request));
    }

    @GetMapping("/{tradeId}/payment")
    public P2PPaymentDtos.PaymentResponse get(
            @PathVariable UUID tradeId, Authentication authentication) {
        return service.getForTrade(userId(authentication), tradeId);
    }

    @PostMapping("/{tradeId}/payment/verify")
    public P2PPaymentDtos.PaymentResponse verify(
            @PathVariable UUID tradeId, Authentication authentication) {
        return service.verify(userId(authentication), tradeId);
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
