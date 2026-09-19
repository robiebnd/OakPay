package com.oakpay.wallet.deposit;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Profile("!prod & !production")
@RequestMapping("/api/v1/wallets/deposit-addresses")
public class TestDepositAddressController {
    private final DepositAddressService service;
    private final String internalSecret;
    public TestDepositAddressController(DepositAddressService service, @Value("${oakpay.internal-secret}") String internalSecret) { this.service = service; this.internalSecret = internalSecret; }

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
        if (suppliedSecret == null || !java.security.MessageDigest.isEqual(
                internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                suppliedSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
        return service.generateTestAddress(request);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
