package com.oakpay.wallet.custody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "oakpay.custody.provider-name", havingValue = "BITGO")
public class BitGoCustodyProvider implements CustodyProvider {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String accessToken;
    private final String walletPassphrase;
    private final Map<String, AssetConfig> assets;

    public BitGoCustodyProvider(
            ObjectMapper objectMapper,
            @Value("${oakpay.custody.bitgo.base-url:https://app.bitgo.com}") String baseUrl,
            @Value("${oakpay.custody.bitgo.access-token:}") String accessToken,
            @Value("${oakpay.custody.bitgo.wallet-passphrase:}") String walletPassphrase,
            @Value("${oakpay.custody.bitgo.btc.wallet-id:}") String btcWalletId,
            @Value("${oakpay.custody.bitgo.btc.coin:btc}") String btcCoin,
            @Value("${oakpay.custody.bitgo.usdt-tron.wallet-id:}") String usdtTronWalletId,
            @Value("${oakpay.custody.bitgo.usdt-tron.coin:trx:usdt}") String usdtTronCoin) {

        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.accessToken = accessToken == null ? "" : accessToken.trim();
        this.walletPassphrase = walletPassphrase == null ? "" : walletPassphrase;

        Map<String, AssetConfig> configured = new LinkedHashMap<>();
        configured.put(key("BTC", "BITCOIN"),
                new AssetConfig("BTC", "BITCOIN", btcCoin.trim(), normalizeWalletId(btcWalletId), 8));
        configured.put(key("USDT", "TRON"),
                new AssetConfig("USDT", "TRON", usdtTronCoin.trim(), normalizeWalletId(usdtTronWalletId), 6));
        this.assets = Map.copyOf(configured);
    }

    @Override
    public String providerName() {
        return "BITGO";
    }

    @Override
    public DepositAddressResult createDepositAddress(DepositAddressRequest request) {
        requireConfigured();

        AssetConfig asset = asset(request.currency(), request.network());
        JsonNode response = request("POST",
                "/api/v2/" + asset.coin() + "/wallet/" + asset.walletId() + "/address",
                "{}");

        String address = text(response, "address");
        String id = textOrNull(response, "id");
        String memoTag = textOrNull(response, "memoId");
        return new DepositAddressResult(id == null ? address : id, address, memoTag);
    }

    @Override
    public WithdrawalResult submitWithdrawal(WithdrawalRequest request) {
        requireConfigured();

        AssetConfig asset = asset(request.currency(), request.network());
        String baseUnits = toBaseUnits(request.amount(), asset.decimals());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("address", request.destinationAddress());
        body.put("amount", baseUnits);
        body.put("sequenceId", request.idempotencyKey());
        if (!walletPassphrase.isBlank()) {
            body.put("walletPassphrase", walletPassphrase);
        }
        if (request.memoTag() != null && !request.memoTag().isBlank()) {
            body.put("memo", request.memoTag().trim());
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize BitGo withdrawal request", e);
        }

        JsonNode response = request("POST",
                "/api/v2/" + asset.coin() + "/wallet/" + asset.walletId() + "/sendcoins",
                payload);

        JsonNode transfer = response.path("transfer");
        String transferId = textOrNull(transfer, "id");
        if (transferId == null) {
            transferId = textOrNull(response, "transfer");
        }
        if (transferId == null) {
            throw new IllegalStateException("BitGo did not return a transfer ID");
        }

        return new WithdrawalResult(transferId, mapTransferStatus(textOrNull(transfer, "state")));
    }

    public JsonNode addTransferWebhook(String coin, String walletId, String url, int confirmations) {
        requireConfigured();
        if (confirmations < 0) throw new IllegalArgumentException("Webhook confirmations cannot be negative");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "transfer");
        body.put("url", url);
        body.put("label", "OakPay custody transfer webhook");
        body.put("numConfirmations", confirmations);
        body.put("listenToFailureStates", true);
        return request("POST",
                "/api/v2/" + requireText(coin, "coin") + "/wallet/" + requireWalletId(walletId) + "/webhooks",
                toJson(body));
    }

    public JsonNode getTransfer(String coin, String walletId, String transferId) {
        requireConfigured();
        String normalizedCoin = requireText(coin, "coin");
        String normalizedWalletId = requireWalletId(walletId);
        String normalizedTransferId = requireText(transferId, "transferId");
        return request("GET", "/api/v2/" + normalizedCoin + "/wallet/" + normalizedWalletId
                + "/transfer/" + normalizedTransferId, "");
    }

    public void verifyWebhook(String webhookId, String signature, String notificationPayload) {
        requireConfigured();
        String id = requireText(webhookId, "webhookId");
        String sig = requireText(signature, "X-Signature-SHA256");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("signature", sig);
        body.put("notificationPayload", notificationPayload);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize BitGo webhook verification request", e);
        }

        // The webhook verification endpoint expects the raw BitGo access token.
        JsonNode response = rawTokenRequest("POST", "/api/v2/webhook/" + id + "/verify", payload);
        if (!response.path("isValid").asBoolean(false)) {
            throw new IllegalArgumentException("BitGo webhook signature is invalid");
        }
    }

    @Override
    public ProviderTransactionStatus getTransactionStatus(String providerReference) {
        requireConfigured();

        RuntimeException lastFailure = null;
        for (AssetConfig asset : assets.values()) {
            try {
                JsonNode response = request("GET",
                        "/api/v2/" + asset.coin() + "/wallet/" + asset.walletId()
                                + "/transfer/" + providerReference,
                        "");
                return mapTransferStatus(textOrNull(response, "state"));
            } catch (RuntimeException e) {
                lastFailure = e;
            }
        }

        throw new IllegalStateException("BitGo transfer was not found in configured wallets: "
                + providerReference, lastFailure);
    }

    private AssetConfig asset(String currency, String network) {
        AssetConfig asset = assets.get(key(currency, network));
        if (asset == null || asset.walletId().isBlank()) {
            throw new IllegalArgumentException("BitGo custody is not configured for "
                    + currency + " on " + network);
        }
        return asset;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize BitGo request", e);
        }
    }

    private JsonNode rawTokenRequest(String method, String path, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            HttpResponse<String> response = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("BitGo webhook verification failed with HTTP "
                        + response.statusCode() + ": " + sanitizeError(response.body()));
            }
            return response.body() == null || response.body().isBlank()
                    ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("BitGo webhook verification interrupted", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException state) throw state;
            throw new IllegalStateException("BitGo webhook verification failed", e);
        }
    }

    private JsonNode request(String method, String path, String body) {
        if (accessToken.isBlank()) {
            throw new IllegalStateException("OAKPAY_BITGO_ACCESS_TOKEN is not configured");
        }

        try {
            String payload = "GET".equals(method) ? "" : body;
            long timestamp = System.currentTimeMillis();
            String hmac = hmacSha256(accessToken, timestamp + "|" + path + "|" + payload);
            String tokenHash = sha256(accessToken);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + tokenHash)
                    .header("Auth-Timestamp", Long.toString(timestamp))
                    .header("BitGo-Auth-Version", "2.0")
                    .header("X-Original-Uri", path)
                    .header("HMAC", hmac);

            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(payload));
            }

            HttpResponse<String> response = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("BitGo API request failed with HTTP "
                        + response.statusCode() + ": " + sanitizeError(response.body()));
            }

            if (response.body() == null || response.body().isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("BitGo API request interrupted", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException state) throw state;
            throw new IllegalStateException("BitGo API request failed", e);
        }
    }

    private ProviderTransactionStatus mapTransferStatus(String state) {
        if (state == null || state.isBlank()) {
            return ProviderTransactionStatus.PROCESSING;
        }

        return switch (state.trim().toLowerCase(Locale.ROOT)) {
            case "confirmed", "complete", "completed" -> ProviderTransactionStatus.COMPLETED;
            case "failed", "rejected", "cancelled", "canceled", "removed", "expired" ->
                    ProviderTransactionStatus.FAILED;
            case "signed", "pending", "pendingapproval", "processing", "delivered" ->
                    ProviderTransactionStatus.PROCESSING;
            default -> ProviderTransactionStatus.PROCESSING;
        };
    }

    private String toBaseUnits(BigDecimal amount, int decimals) {
        BigDecimal scaled = amount.movePointRight(decimals);
        try {
            return scaled.toBigIntegerExact().toString();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amount has more than " + decimals
                    + " decimal places for the BitGo asset", e);
        }
    }

    private void requireConfigured() {
        if (accessToken.isBlank()) {
            throw new IllegalStateException("OAKPAY_BITGO_ACCESS_TOKEN is not configured");
        }
    }

    private String normalizeWalletId(String walletId) {
        if (walletId == null || walletId.isBlank()) return "";
        String normalized = walletId.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[0-9a-f]{32}$")) {
            throw new IllegalStateException("Invalid BitGo wallet ID");
        }
        return normalized;
    }

    private String key(String currency, String network) {
        return currency.trim().toUpperCase(Locale.ROOT) + ":" + network.trim().toUpperCase(Locale.ROOT);
    }

    private String text(JsonNode node, String field) {
        String value = textOrNull(node, field);
        if (value == null) {
            throw new IllegalStateException("BitGo response did not contain " + field);
        }
        return value;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) return null;
        return value.asText();
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private String requireWalletId(String walletId) {
        String normalized = requireText(walletId, "walletId").toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[0-9a-f]{32}$")) {
            throw new IllegalArgumentException("Invalid BitGo wallet ID");
        }
        return normalized;
    }

    private String sanitizeError(String body) {
        if (body == null || body.isBlank()) return "empty response";
        return body.length() > 1000 ? body.substring(0, 1000) : body;
    }

    private String trimTrailingSlash(String value) {
        String normalized = value == null || value.isBlank() ? "https://app.bitgo.com" : value.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String sha256(String value) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String hmacSha256(String key, String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return hex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private record AssetConfig(String currency, String network, String coin, String walletId, int decimals) {}
}
