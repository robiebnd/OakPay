package com.oakpay.wallet.api;

import com.oakpay.wallet.wallet.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletDtos.WalletResponse> createWallet(
            @Valid @RequestBody WalletDtos.CreateWalletRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(userId(authentication), request.currency()));
    }

    @PostMapping("/{currency}/deposit")
    public WalletDtos.WalletResponse deposit(
            @PathVariable String currency,
            @Valid @RequestBody WalletDtos.DepositRequest request,
            Authentication authentication) {
        return walletService.deposit(userId(authentication), currency, request);
    }

    @GetMapping
    public List<WalletDtos.WalletResponse> getWallets(Authentication authentication) {
        return walletService.getUserWallets(userId(authentication));
    }

    @GetMapping("/{currency}")
    public WalletDtos.WalletResponse getWallet(@PathVariable String currency, Authentication authentication) {
        return walletService.getWallet(userId(authentication), currency);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
