package com.oakpay.wallet.custody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets/custody/bitgo")
public class BitGoWebhookRegistrationController {
    private final BitGoCustodyProvider bitGo;
    private final String internalSecret;
    private final String webhookBaseUrl;
    private final String btcWalletId;
    private final String btcCoin;
    private final String usdtTronWalletId;
    private final String usdtTronCoin;
    private final int btcConfirmations;
    private final int usdtTronConfirmations;

    public BitGoWebhookRegistrationController(
            BitGoCustodyProvider bitGo,
            @Value("${oakpay.internal-secret}") String internalSecret,
            @Value("${oakpay.custody.bitgo.webhook-base-url:}") String webhookBaseUrl,
            @Value("${oakpay.custody.bitgo.btc.wallet-id:}") String btcWalletId,
            @Value("${oakpay.custody.bitgo.btc.coin:btc}") String btcCoin,
            @Value("${oakpay.custody.bitgo.usdt-tron.wallet-id:}") String usdtTronWalletId,
            @Value("${oakpay.custody.bitgo.usdt-tron.coin:trx:usdt}") String usdtTronCoin,
            @Value("${oakpay.custody.bitgo.btc.required-confirmations:3}") int btcConfirmations,
            @Value("${oakpay.custody.bitgo.usdt-tron.required-confirmations:20}") int usdtTronConfirmations) {
        this.bitGo = bitGo;
        this.internalSecret = internalSecret;
        this.webhookBaseUrl = webhookBaseUrl == null ? "" : webhookBaseUrl.trim();
        this.btcWalletId = btcWalletId;
        this.btcCoin = btcCoin;
        this.usdtTronWalletId = usdtTronWalletId;
        this.usdtTronCoin = usdtTronCoin;
        this.btcConfirmations = btcConfirmations;
        this.usdtTronConfirmations = usdtTronConfirmations;
    }

    @PostMapping("/webhooks/register")
    public Map<String, Object> register(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String providedSecret) {

        if (internalSecret == null || internalSecret.isBlank()
                || !internalSecret.equals(providedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
        if (webhookBaseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "OAKPAY_BITGO_WEBHOOK_BASE_URL is not configured");
        }

        String base = trimTrailingSlash(webhookBaseUrl);
        try {
            JsonResult btc = registerAsset(
                    btcCoin, btcWalletId, base + "/api/v1/webhooks/bitgo/" + btcCoin + "/transfers",
                    btcConfirmations);
            JsonResult usdt = registerAsset(
                    usdtTronCoin, usdtTronWalletId,
                    base + "/api/v1/webhooks/bitgo/" + usdtTronCoin + "/transfers",
                    usdtTronConfirmations);

            return Map.of("provider", "BITGO", "btc", btc.value(), "usdtTron", usdt.value());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = e.getClass().getSimpleName();
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "BitGo webhook registration failed: " + sanitizeMessage(message),
                    e);
        }
    }

    private JsonResult registerAsset(String coin, String walletId, String url, int confirmations) {
        if (walletId == null || walletId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "BitGo wallet ID is not configured for " + coin);
        }
        if (confirmations < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Required confirmations must be at least 1");
        }
        return new JsonResult(bitGo.addTransferWebhook(coin, walletId, url, confirmations));
    }

    private String sanitizeMessage(String message) {
        String sanitized = message.replaceAll(
                "(?i)(authorization|bearer|token|secret|passphrase)\\s*[:=]\\s*[^,; ]+",
                "$1=[REDACTED]");
        return sanitized.length() > 1000 ? sanitized.substring(0, 1000) : sanitized;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record JsonResult(Object value) {}
}
