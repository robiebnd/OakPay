package com.oakpay.trading.p2p;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/p2p/payment-methods")
public class PaymentMethodController {
    @GetMapping
    public List<String> supportedMethods() {
        return List.of(
                "ECOCASH",
                "INNBUCKS",
                "ZIPIT",
                "BANK_TRANSFER",
                "MOBILE_MONEY",
                "CASH_DEPOSIT"
        );
    }
}
