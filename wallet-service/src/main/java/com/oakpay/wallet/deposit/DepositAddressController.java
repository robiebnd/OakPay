package com.oakpay.wallet.deposit;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/deposit-addresses")
public class DepositAddressController {
    private final DepositAddressService service;
    private final String internalSecret;

    public DepositAddressController(DepositAddressService service,
                                    @Value("${oakpay.internal-secret}") String internalSecret) {
        this.service = service;
        this.internalSecret = internalSecret;
    }

    @GetMapping
    public List<DepositAddressDtos.DepositAddressResponse> getAddresses(Authentication authentication) {
        return service.getActiveAddresses(userId(authentication));
    }

    @GetMapping("/{currency}/{network}")
    public DepositAddressDtos.DepositAddressResponse getAddress(
            @PathVariable String currency,
            @PathVariable String network,
            Authentication authentication) {
        return service.getAddress(userId(authentication), currency, network);
    }

    /** Trusted custody/address-provider integration only. */
    @PostMapping("/internal/assign")
    public DepositAddressDtos.DepositAddressResponse assign(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @Valid @RequestBody DepositAddressDtos.AssignAddressRequest request) {
        requireInternalSecret(suppliedSecret);
        return service.assign(request);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || internalSecret == null || !MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8), suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
    }
}
