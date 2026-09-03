package com.oakpay.wallet.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets/internal/escrow")
public class EscrowController {
    private final EscrowService escrowService;
    private final String secret;

    public EscrowController(EscrowService escrowService,
                            @Value("${oakpay.internal-secret}") String secret) {
        this.escrowService = escrowService;
        this.secret = secret;
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String supplied,
                        @RequestBody InternalWalletDtos.EscrowReleaseRequest request) {
        if (secret == null || secret.isBlank() || !secret.equals(supplied)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal credential");
        }
        escrowService.release(request.sellerId(), request.buyerId(), request.asset(), request.amount(), request.reference());
    }
}
