package com.oakpay.trading.p2p;

import com.oakpay.trading.security.KycStatusClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

@Service
public class TradeLimitService {
    private static final BigDecimal DEFAULT_UNVERIFIED_USD_LIMIT = new BigDecimal("50.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final KycStatusClient kycStatusClient;
    private final P2PExchangeRateRepository exchangeRateRepository;
    private final PlatformFeeService platformFeeService;

    public TradeLimitService(
            KycStatusClient kycStatusClient,
            P2PExchangeRateRepository exchangeRateRepository,
            PlatformFeeService platformFeeService) {
        this.kycStatusClient = kycStatusClient;
        this.exchangeRateRepository = exchangeRateRepository;
        this.platformFeeService = platformFeeService;
    }

    public void validate(UUID userId, String fiatCurrency, BigDecimal fiatAmount) {
        if (kycStatusClient.isVerified(userId)) return;
        BigDecimal usdAmount = toUsd(fiatCurrency, fiatAmount);
        BigDecimal unverifiedUsdLimit = platformFeeService.unverifiedUsdLimit();
        if (usdAmount.compareTo(unverifiedUsdLimit) > 0) {
            throw new IllegalArgumentException(
                    String.format(Locale.ROOT,
                            "KYC verification required. Unverified accounts can trade up to USD %s per transaction. Your trade is approximately USD %s.",
                            unverifiedUsdLimit.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            usdAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()));
        }
    }

    public BigDecimal maximumFiatAmount(UUID userId, String fiatCurrency) {
        if (kycStatusClient.isVerified(userId)) return null;
        return fromUsd(fiatCurrency, platformFeeService.unverifiedUsdLimit());
    }

    private BigDecimal toUsd(String fiatCurrency, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) return ZERO;
        String fiat = fiatCurrency == null ? "" : fiatCurrency.trim().toUpperCase(Locale.ROOT);
        if ("USD".equals(fiat)) return amount;
        if ("ZWG".equals(fiat)) {
            BigDecimal usdToZwg = usdToZwgRate();
            return amount.divide(usdToZwg, 8, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("Unsupported fiat currency for KYC limit: " + fiatCurrency);
    }

    private BigDecimal fromUsd(String fiatCurrency, BigDecimal usdAmount) {
        String fiat = fiatCurrency == null ? "" : fiatCurrency.trim().toUpperCase(Locale.ROOT);
        if ("USD".equals(fiat)) return usdAmount.setScale(2, RoundingMode.HALF_UP);
        if ("ZWG".equals(fiat)) {
            return usdAmount.multiply(usdToZwgRate()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("Unsupported fiat currency for KYC limit: " + fiatCurrency);
    }

    private BigDecimal usdToZwgRate() {
        BigDecimal usdtToUsd = exchangeRateRepository
                .findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc("USDT", "USD")
                .map(P2PExchangeRate::getRate)
                .orElse(BigDecimal.ONE);
        BigDecimal usdtToZwg = exchangeRateRepository
                .findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc("USDT", "ZWG")
                .map(P2PExchangeRate::getRate)
                .orElseThrow(() -> new IllegalStateException("No active USDT/ZWG exchange rate is configured"));
        return usdtToZwg.divide(usdtToUsd, 8, RoundingMode.HALF_UP);
    }
}
