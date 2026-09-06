package com.oakpay.trading.p2p;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class P2PExchangeRateService {
    private final P2PExchangeRateRepository repository;

    public P2PExchangeRateService(P2PExchangeRateRepository repository) {
        this.repository = repository;
    }

    public P2PExchangeRate getRate(String baseCurrency, String quoteCurrency) {
        String base = normalize(baseCurrency);
        String quote = normalize(quoteCurrency);
        return repository.findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc(base, quote)
                .orElseThrow(() -> new IllegalArgumentException("No fixed market rate configured for " + base + "/" + quote));
    }

    public BigDecimal getRateValue(String baseCurrency, String quoteCurrency) {
        return getRate(baseCurrency, quoteCurrency).getRate();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
