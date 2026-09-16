package com.oakpay.trading.p2p;

import com.oakpay.trading.security.KycStatusClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeLimitServiceTest {

    @Mock KycStatusClient kycStatusClient;
    @Mock P2PExchangeRateRepository exchangeRateRepository;

    @Test
    void unverifiedUserCanTradeExactlyFiftyUsd() {
        UUID userId = UUID.randomUUID();
        when(kycStatusClient.isVerified(userId)).thenReturn(false);

        TradeLimitService service = new TradeLimitService(
                kycStatusClient, exchangeRateRepository, new BigDecimal("50.00"));

        assertDoesNotThrow(() -> service.validate(userId, "USD", new BigDecimal("50.00")));
    }

    @Test
    void unverifiedUserCannotTradeAboveFiftyUsd() {
        UUID userId = UUID.randomUUID();
        when(kycStatusClient.isVerified(userId)).thenReturn(false);

        TradeLimitService service = new TradeLimitService(
                kycStatusClient, exchangeRateRepository, new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.validate(userId, "USD", new BigDecimal("50.01")));
    }

    @Test
    void verifiedUserIsNotSubjectToFiftyUsdLimit() {
        UUID userId = UUID.randomUUID();
        when(kycStatusClient.isVerified(userId)).thenReturn(true);

        TradeLimitService service = new TradeLimitService(
                kycStatusClient, exchangeRateRepository, new BigDecimal("50.00"));

        assertDoesNotThrow(() -> service.validate(userId, "USD", new BigDecimal("5000.00")));
    }

    @Test
    void unverifiedZwgTradeUsesActiveUsdtReferenceRates() {
        UUID userId = UUID.randomUUID();
        when(kycStatusClient.isVerified(userId)).thenReturn(false);
        when(exchangeRateRepository.findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc("USDT", "USD"))
                .thenReturn(Optional.of(rate("USDT", "USD", "1.00")));
        when(exchangeRateRepository.findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc("USDT", "ZWG"))
                .thenReturn(Optional.of(rate("USDT", "ZWG", "26.56")));

        TradeLimitService service = new TradeLimitService(
                kycStatusClient, exchangeRateRepository, new BigDecimal("50.00"));

        assertDoesNotThrow(() -> service.validate(userId, "ZWG", new BigDecimal("1328.00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.validate(userId, "ZWG", new BigDecimal("1328.01")));
    }

    private P2PExchangeRate rate(String base, String quote, String value) {
        return new P2PExchangeRate() {
            @Override public String getBaseCurrency() { return base; }
            @Override public String getQuoteCurrency() { return quote; }
            @Override public BigDecimal getRate() { return new BigDecimal(value); }
        };
    }
}
