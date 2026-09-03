package com.oakpay.trading.market;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradingPairRepository extends JpaRepository<TradingPair, UUID> {
    Optional<TradingPair> findBySymbolIgnoreCase(String symbol);
    Optional<TradingPair> findByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCase(String baseCurrency, String quoteCurrency);
    List<TradingPair> findAllByStatusOrderBySymbolAsc(TradingPairStatus status);
}
