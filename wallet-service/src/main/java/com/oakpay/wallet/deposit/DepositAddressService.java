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
        UUID userId = parseUserId(request.userId());
        String asset = normalize(request.currency()), chain = normalize(request.network());
        String address = request.address().trim();
        if (address.length() < 8) throw new IllegalArgumentException("Deposit address is invalid");
        DepositAddress entity = repository.findByUserIdAndCurrencyAndNetworkAndStatus(userId, asset, chain, DepositAddressStatus.ACTIVE)
                .orElseGet(DepositAddress::new);
        entity.setUserId(userId); entity.setCurrency(asset); entity.setNetwork(chain); entity.setAddress(address);
        entity.setMemoTag(request.memoTag()); entity.setStatus(DepositAddressStatus.ACTIVE);
        return DepositAddressDtos.DepositAddressResponse.from(repository.save(entity));
    }

    /**
     * Generates a development-only address for local end-to-end testing.
     * This is deliberately NOT a real blockchain address and must never be used in production.
     */
    @Transactional
    public DepositAddressDtos.DepositAddressResponse generateTestAddress(DepositAddressDtos.GenerateTestAddressRequest request) {
        UUID userId = parseUserId(request.userId());
        String asset = normalize(request.currency()), chain = normalize(request.network());

        DepositAddress existing = repository
                .findByUserIdAndCurrencyAndNetworkAndStatus(userId, asset, chain, DepositAddressStatus.ACTIVE)
                .orElse(null);
        if (existing != null) {
            return DepositAddressDtos.DepositAddressResponse.from(existing);
        }

        DepositAddress entity = new DepositAddress();
        entity.setUserId(userId);
        entity.setCurrency(asset);
        entity.setNetwork(chain);
        entity.setAddress("OAKTEST-" + chain + "-" + asset + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase());
        entity.setStatus(DepositAddressStatus.ACTIVE);
        return DepositAddressDtos.DepositAddressResponse.from(repository.save(entity));
    }

    private UUID parseUserId(String value) {
        try { return UUID.fromString(value); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid userId"); }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase();
    }
}
