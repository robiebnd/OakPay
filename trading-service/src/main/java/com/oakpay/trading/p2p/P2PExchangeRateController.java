package com.oakpay.trading.p2p;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/p2p/rates")
public class P2PExchangeRateController {
    private final P2PExchangeRateService service;

    public P2PExchangeRateController(P2PExchangeRateService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<RateResponse> getRate(
            @RequestParam String baseCurrency,
            @RequestParam String quoteCurrency) {
        P2PExchangeRate rate = service.getRate(baseCurrency, quoteCurrency);
        return ResponseEntity.ok(RateResponse.from(rate));
    }

    public record RateResponse(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            LocalDateTime effectiveAt) {
        static RateResponse from(P2PExchangeRate rate) {
            return new RateResponse(
                    rate.getBaseCurrency(),
                    rate.getQuoteCurrency(),
                    rate.getRate(),
                    rate.getSource(),
                    rate.getEffectiveAt());
        }
    }
}
