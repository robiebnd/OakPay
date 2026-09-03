package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class P2PCommissionService {
    private final P2PCommissionRepository repository;
    private final BigDecimal rate;

    public P2PCommissionService(P2PCommissionRepository repository,
                                 @Value("${oakpay.p2p.commission-rate:0.001}") BigDecimal rate) {
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("P2P commission rate must be between 0 and 1");
        }
        this.repository = repository;
        this.rate = rate;
    }

    @Transactional
    public P2PCommission assess(P2PTrade trade) {
        return repository.findByTradeId(trade.getId()).orElseGet(() -> {
            BigDecimal amount = trade.getFiatAmount().multiply(rate).setScale(18, RoundingMode.DOWN);
            P2PCommission commission = new P2PCommission();
            commission.setTradeId(trade.getId());
            commission.setPayerId(trade.getSellerId());
            commission.setFiatCurrency(trade.getFiatCurrency());
            commission.setFiatAmount(trade.getFiatAmount());
            commission.setRate(rate);
            commission.setCommissionAmount(amount);
            commission.setStatus(P2PCommissionStatus.ASSESSED);
            return repository.save(commission);
        });
    }

    @Transactional(readOnly = true)
    public P2PCommission getByTrade(UUID tradeId) {
        return repository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Commission record not found"));
    }
}
