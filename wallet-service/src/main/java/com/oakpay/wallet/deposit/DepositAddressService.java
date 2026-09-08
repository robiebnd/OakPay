package com.oakpay.wallet.deposit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DepositAddressService {
    private final DepositAddressRepository repository;

    public DepositAddressService(DepositAddressRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<DepositAddressDtos.DepositAddressResponse> getActiveAddresses(UUID userId) {
        return repository.findAllByUserIdAndStatusOrderByCurrencyAscNetworkAsc(userId, DepositAddressStatus.ACTIVE)
                .stream().map(DepositAddressDtos.DepositAddressResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DepositAddressDtos.DepositAddressResponse> getActiveAddresses(UUID userId, String currency) {
        return repository.findAllByUserIdAndCurrencyAndStatusOrderByNetworkAsc(userId, normalize(currency), DepositAddressStatus.ACTIVE)
                .stream().map(DepositAddressDtos.DepositAddressResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DepositAddressDtos.DepositAddressResponse getAddress(UUID userId, String currency, String network) {
        String asset = normalize(currency), chain = normalize(network);
        return repository.findByUserIdAndCurrencyAndNetworkAndStatus(userId, asset, chain, DepositAddressStatus.ACTIVE)
                .map(DepositAddressDtos.DepositAddressResponse::from)
                .orElseThrow(() -> new IllegalStateException("No deposit address is currently assigned for " + asset + " on " + chain));
    }

    /** Called by a trusted custody integration after it creates/assigns a real blockchain address. */
    @Transactional
    public DepositAddressDtos.DepositAddressResponse assign(DepositAddressDtos.AssignAddressRequest request) {
        UUID userId;
        try { userId = UUID.fromString(request.userId()); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid userId"); }
        String asset = normalize(request.currency()), chain = normalize(request.network());
        String address = request.address().trim();
        if (address.length() < 8) throw new IllegalArgumentException("Deposit address is invalid");
        DepositAddress entity = repository.findByUserIdAndCurrencyAndNetworkAndStatus(userId, asset, chain, DepositAddressStatus.ACTIVE)
                .orElseGet(DepositAddress::new);
        entity.setUserId(userId); entity.setCurrency(asset); entity.setNetwork(chain); entity.setAddress(address);
        entity.setMemoTag(request.memoTag()); entity.setStatus(DepositAddressStatus.ACTIVE);
        return DepositAddressDtos.DepositAddressResponse.from(repository.save(entity));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase();
    }
}
