package com.oakpay.trading.p2p;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/trades")
public class P2PTradeController {
    private final P2PTradeService service;

    public P2PTradeController(P2PTradeService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<P2PTradeDtos.TradeResponse> create(
            @RequestBody P2PTradeDtos.CreateRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId(authentication), request));
    }

    @GetMapping
    public List<P2PTradeDtos.TradeResponse> mine(Authentication authentication) {
        return service.mine(userId(authentication));
    }

    @GetMapping("/{tradeId}")
    public P2PTradeDtos.TradeResponse get(@PathVariable UUID tradeId, Authentication authentication) {
        return service.getOne(userId(authentication), tradeId);
    }

    @PostMapping("/{tradeId}/paid")
    public P2PTradeDtos.TradeResponse paid(@PathVariable UUID tradeId,
                                           @RequestBody P2PTradeDtos.PaymentRequest request,
                                           Authentication authentication) {
        return service.markPaid(userId(authentication), tradeId, request);
    }

    @PostMapping("/{tradeId}/confirm")
    public P2PTradeDtos.TradeResponse confirm(@PathVariable UUID tradeId, Authentication authentication) {
        return service.confirmPayment(userId(authentication), tradeId);
    }

    @PostMapping("/{tradeId}/cancel")
    public P2PTradeDtos.TradeResponse cancel(@PathVariable UUID tradeId, Authentication authentication) {
        return service.cancel(userId(authentication), tradeId);
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
