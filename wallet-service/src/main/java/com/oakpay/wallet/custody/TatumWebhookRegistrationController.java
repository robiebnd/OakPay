package com.oakpay.wallet.custody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
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
            @Value("${oakpay.internal-secret}") String configuredSecret) {

        if (!configuredSecret.equals(internalSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Unauthorized"));
        }

        try {
            provider.enableWebhookHmac();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("provider", "TATUM");
            response.put("message", "Tatum webhook HMAC enabled. Deposit-address alerts are created when OakPay generates addresses.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 502);
            response.put("provider", "TATUM");
            response.put("message", "Tatum webhook configuration failed");
            response.put("detail", sanitize(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        }
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown Tatum provider error";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}