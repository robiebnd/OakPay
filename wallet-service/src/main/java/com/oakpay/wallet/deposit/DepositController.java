package com.oakpay.wallet.deposit;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/deposits")
public class DepositController {
    private final DepositService depositService;
    private final String internalSecret;

    public DepositController(DepositService depositService,
                             @Value("${oakpay.internal-secret}") String internalSecret) {
        this.depositService = depositService;
        this.internalSecret = internalSecret;
    }

    @GetMapping
    public List<DepositDtos.DepositResponse> getDeposits(Authentication authentication) {
        return depositService.getUserDeposits(userId(authentication));
    }

    /** Webhook endpoint for a trusted custody/blockchain integration. */
    @PostMapping("/internal/webhook")
    @ResponseStatus(HttpStatus.OK)
    public DepositDtos.DepositResponse webhook(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @Valid @RequestBody DepositDtos.BlockchainDepositWebhook request) {
        if (suppliedSecret == null || !constantTimeEquals(internalSecret, suppliedSecret)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
        return depositService.processWebhook(request);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return java.security.MessageDigest.isEqual(
                expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
