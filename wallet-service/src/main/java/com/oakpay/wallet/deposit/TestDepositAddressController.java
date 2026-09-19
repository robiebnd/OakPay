package com.oakpay.wallet.deposit;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Profile("!prod")
@RequestMapping("/api/v1/wallets/deposit-addresses")
public class TestDepositAddressController {
    private final DepositAddressService service;
    public TestDepositAddressController(DepositAddressService service) { this.service = service; }

    @PostMapping("/test")
    public DepositAddressDtos.DepositAddressResponse generateTestAddressForCurrentUser(
            @Valid @RequestBody DepositAddressDtos.TestAddressRequest request,
            Authentication authentication) {
        return service.generateTestAddress(new DepositAddressDtos.GenerateTestAddressRequest(
                userId(authentication).toString(), request.currency(), request.network()));
    }

    @PostMapping("/internal/generate-test")
    public DepositAddressDtos.DepositAddressResponse generateTestAddress(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @Valid @RequestBody DepositAddressDtos.GenerateTestAddressRequest request) {
        String expected = System.getProperty("oakpay.internal-secret");
        if (expected == null || expected.isBlank()) {
            // Spring's property is injected into the production controller; this endpoint is non-prod only.
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Internal test-address secret is not configured");
        }
        if (suppliedSecret == null || !java.security.MessageDigest.isEqual(
                expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                suppliedSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
        return service.generateTestAddress(request);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
