package com.oakpay.wallet.custody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets/custody/tatum")
@ConditionalOnProperty(name = "oakpay.custody.provider-name", havingValue = "TATUM", matchIfMissing = true)
public class TatumWebhookRegistrationController {
    private final TatumCustodyProvider provider;

    public TatumWebhookRegistrationController(TatumCustodyProvider provider) {
        this.provider = provider;
    }

    @PostMapping("/webhooks/configure")
    public ResponseEntity<Map<String, Object>> configure(
            @RequestHeader("X-OakPay-Internal-Secret") String internalSecret,
            @Value("\${oakpay.internal-secret}") String configuredSecret) {
        if (!configuredSecret.equals(internalSecret)) {
            return ResponseEntity.status(401).body(Map.of("status", 401, "message", "Unauthorized"));
        }

        provider.enableWebhookHmac();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("provider", "TATUM");
        response.put("message", "Tatum webhook HMAC enabled. Deposit-address alerts are created when OakPay generates addresses.");
        return ResponseEntity.ok(response);
    }
}
