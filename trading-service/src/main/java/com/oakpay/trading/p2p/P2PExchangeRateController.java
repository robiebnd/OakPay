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
        return ResponseEntity.ok(RateResponse.from(service.getRate(baseCurrency, quoteCurrency)));
    }

    public record RateResponse(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            LocalDateTime effectiveAt) {
        static RateResponse from(P2PExchangeRateService.RateSnapshot rate) {
            return new RateResponse(
                    rate.baseCurrency(),
                    rate.quoteCurrency(),
                    rate.rate(),
                    rate.source(),
                    rate.effectiveAt());
        }
    }
}
