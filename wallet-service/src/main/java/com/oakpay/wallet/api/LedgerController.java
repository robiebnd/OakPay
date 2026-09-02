package com.oakpay.wallet.api;

import com.oakpay.wallet.ledger.LedgerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/{currency}/deposit")
    public LedgerDtos.LedgerResponse deposit(@PathVariable String currency,
                                              @Valid @RequestBody LedgerDtos.BalanceRequest request,
                                              Authentication authentication) {
        return ledgerService.deposit(userId(authentication), currency, request);
    }

    @PostMapping("/{currency}/withdraw")
    public LedgerDtos.LedgerResponse withdraw(@PathVariable String currency,
                                               @Valid @RequestBody LedgerDtos.BalanceRequest request,
                                               Authentication authentication) {
        return ledgerService.withdraw(userId(authentication), currency, request);
    }

    @GetMapping("/transactions")
    public List<LedgerDtos.LedgerResponse> transactions(
            @RequestParam(required = false) String currency,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {
        return ledgerService.getTransactions(userId(authentication), currency, limit);
    }

    @GetMapping("/{currency}/transactions")
    public List<LedgerDtos.LedgerResponse> currencyTransactions(
            @PathVariable String currency,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {
        return ledgerService.getTransactions(userId(authentication), currency, limit);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
