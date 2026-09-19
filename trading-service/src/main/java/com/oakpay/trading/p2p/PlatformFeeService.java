package com.oakpay.trading.p2p;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

import com.oakpay.trading.security.AuditLogClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformFeeService {
    public static final String TRADING_FEE = "TRADING_FEE_RATE";
    public static final String P2P_COMMISSION = "P2P_COMMISSION_RATE";
    public static final String UNVERIFIED_LIMIT = "P2P_UNVERIFIED_USD_LIMIT";

    private final PlatformFeeSettingRepository repository;
    private final BigDecimal defaultTradingFee;
    private final BigDecimal defaultP2pCommission;
    private final BigDecimal defaultUnverifiedLimit;
    private final AuditLogClient auditLogClient;

    public PlatformFeeService(
            PlatformFeeSettingRepository repository,
            @Value("${oakpay.trading.fee-rate:0.001}") BigDecimal defaultTradingFee,
            @Value("${oakpay.p2p.commission-rate:0.001}") BigDecimal defaultP2pCommission,
            @Value("${oakpay.p2p.unverified-usd-limit:50.00}") BigDecimal defaultUnverifiedLimit,
            AuditLogClient auditLogClient) {
        validateRate(defaultTradingFee, "Trading fee");
        validateRate(defaultP2pCommission, "P2P commission");
        if (defaultUnverifiedLimit == null || defaultUnverifiedLimit.signum() <= 0) {
            throw new IllegalArgumentException("Unverified trade limit must be greater than zero");
        }
        this.repository = repository;
        this.defaultTradingFee = defaultTradingFee;
        this.defaultP2pCommission = defaultP2pCommission;
        this.defaultUnverifiedLimit = defaultUnverifiedLimit.setScale(2, RoundingMode.HALF_UP);
        this.auditLogClient = auditLogClient;
    }

    @Transactional(readOnly = true)
    public BigDecimal tradingFeeRate() { return read(TRADING_FEE, defaultTradingFee); }

    @Transactional(readOnly = true)
    public BigDecimal p2pCommissionRate() { return read(P2P_COMMISSION, defaultP2pCommission); }

    @Transactional(readOnly = true)
    public BigDecimal unverifiedUsdLimit() { return read(UNVERIFIED_LIMIT, defaultUnverifiedLimit); }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> current() {
        return Map.of(
                TRADING_FEE, tradingFeeRate(),
                P2P_COMMISSION, p2pCommissionRate(),
                UNVERIFIED_LIMIT, unverifiedUsdLimit()
        );
    }

    @Transactional
    public BigDecimal set(String key, BigDecimal value, UUID actor) {
        validateKey(key);
        if (value == null) throw new IllegalArgumentException("Fee setting value is required");
        if (UNVERIFIED_LIMIT.equals(key)) {
            if (value.signum() <= 0 || value.compareTo(new BigDecimal("1000000")) > 0)
                throw new IllegalArgumentException("Unverified trade limit is outside the allowed range");
            value = value.setScale(2, RoundingMode.HALF_UP);
        } else {
            validateRate(value, "Fee");
            value = value.setScale(10, RoundingMode.HALF_UP);
        }
        PlatformFeeSetting setting = repository.findBySettingKey(key).orElseGet(PlatformFeeSetting::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setUpdatedBy(actor);
        BigDecimal saved = repository.save(setting).getSettingValue();
        auditLogClient.record(actor, "PLATFORM_FEE_CHANGED", "PLATFORM_FEE", key, "SUCCESS", "{\"value\":"+saved.toPlainString()+"}");
        return saved;
    }

    private BigDecimal read(String key, BigDecimal fallback) {
        return repository.findBySettingKey(key).map(PlatformFeeSetting::getSettingValue).orElse(fallback);
    }

    private void validateKey(String key) {
        if (!TRADING_FEE.equals(key) && !P2P_COMMISSION.equals(key) && !UNVERIFIED_LIMIT.equals(key))
            throw new IllegalArgumentException("Unsupported platform setting");
    }

    private void validateRate(BigDecimal value, String label) {
        if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0)
            throw new IllegalArgumentException(label + " rate must be between 0 and 1");
    }
}
