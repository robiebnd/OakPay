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
    private final boolean testAddressesEnabled;

    public DepositAddressController(DepositAddressService service,
                                    @Value("${oakpay.internal-secret}") String internalSecret,
                                    @Value("${oakpay.test-addresses.enabled:false}") boolean testAddressesEnabled) {
        this.service = service;
        this.internalSecret = internalSecret;
        this.testAddressesEnabled = testAddressesEnabled;
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

    /**
     * Development-only authenticated endpoint used by the mobile app to provision
     * a local test address for the signed-in user. No user id is accepted from the client.
     */
    @PostMapping("/test")
    public DepositAddressDtos.DepositAddressResponse generateTestAddressForCurrentUser(
            @Valid @RequestBody DepositAddressDtos.TestAddressRequest request,
            Authentication authentication) {
        if (!testAddressesEnabled) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Test deposit addresses are disabled");
        }
        return service.generateTestAddress(new DepositAddressDtos.GenerateTestAddressRequest(
                userId(authentication).toString(), request.currency(), request.network()));
    }

    /** Trusted custody/address-provider integration only. */
    @PostMapping("/internal/assign")
    public DepositAddressDtos.DepositAddressResponse assign(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @Valid @RequestBody DepositAddressDtos.AssignAddressRequest request) {
        requireInternalSecret(suppliedSecret);
        return service.assign(request);
    }

    /**
     * Development-only endpoint. Generates a clearly-marked non-blockchain address
     * so local Postman/mobile end-to-end deposit tests can run before custody integration exists.
     */
    @PostMapping("/internal/generate-test")
    public DepositAddressDtos.DepositAddressResponse generateTestAddress(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @Valid @RequestBody DepositAddressDtos.GenerateTestAddressRequest request) {
        requireInternalSecret(suppliedSecret);
        if (!testAddressesEnabled) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Test deposit addresses are disabled");
        }
        return service.generateTestAddress(request);
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
