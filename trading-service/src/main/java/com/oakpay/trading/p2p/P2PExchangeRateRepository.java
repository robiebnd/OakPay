package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface P2PExchangeRateRepository extends JpaRepository<P2PExchangeRate, UUID> {
    Optional<P2PExchangeRate> findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc(
            String baseCurrency, String quoteCurrency);
}
