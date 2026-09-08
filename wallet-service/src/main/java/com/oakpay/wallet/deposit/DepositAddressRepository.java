package com.oakpay.wallet.deposit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositAddressRepository extends JpaRepository<DepositAddress, UUID> {
    List<DepositAddress> findAllByUserIdAndStatusOrderByCurrencyAscNetworkAsc(UUID userId, DepositAddressStatus status);
    Optional<DepositAddress> findByUserIdAndCurrencyAndNetworkAndStatus(UUID userId, String currency, String network, DepositAddressStatus status);
}
