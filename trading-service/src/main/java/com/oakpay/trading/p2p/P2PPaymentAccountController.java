package com.oakpay.trading.p2p;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/payment-accounts")
public class P2PPaymentAccountController {
    private final P2PPaymentAccountService service;

    public P2PPaymentAccountController(P2PPaymentAccountService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<P2PPaymentAccountDtos.Response> create(
            @RequestBody P2PPaymentAccountDtos.CreateRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId(authentication), request));
    }

    @GetMapping
    public List<P2PPaymentAccountDtos.Response> mine(Authentication authentication) {
        return service.mine(userId(authentication));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID accountId, Authentication authentication) {
        service.deactivate(userId(authentication), accountId);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
