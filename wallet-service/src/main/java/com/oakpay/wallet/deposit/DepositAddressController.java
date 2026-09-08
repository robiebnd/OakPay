package com.oakpay.wallet.deposit;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/deposit-addresses")
public class DepositAddressController {
    private final DepositAddressService service;

    public DepositAddressController(DepositAddressService service) { this.service = service; }

    @GetMapping
    public List<DepositAddressDtos.DepositAddressResponse> getAddresses(Authentication authentication) {
        return service.getActiveAddresses(userId(authentication));
    }

    @GetMapping("/{currency}")
    public List<DepositAddressDtos.DepositAddressResponse> getAddressesForAsset(
            @PathVariable String currency, Authentication authentication) {
        return service.getActiveAddresses(userId(authentication), currency);
    }

    @GetMapping("/{currency}/{network}")
    public DepositAddressDtos.DepositAddressResponse getAddress(
            @PathVariable String currency, @PathVariable String network, Authentication authentication) {
        return service.getAddress(userId(authentication), currency, network);
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
