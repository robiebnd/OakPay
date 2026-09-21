package com.oakpay.wallet.custody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "oakpay.custody.provider-name", havingValue = "TATUM", matchIfMissing = true)
public class TatumCustodyProvider implements CustodyProvider {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final TatumAddressIndexRepository indexRepository;
    private final String apiKey;
    private final String baseUrl;
    private final String webhookBaseUrl;
    private final String webhookHmacSecret;
    private final boolean testnet;
    private final int btcRequiredConfirmations;
    private final int usdtRequiredConfirmations;
    private final String btcXpub;
    private final String tronXpub;
    private final String btcSignatureId;
    private final String btcSourceAddress;
    private final String usdtSignatureId;
    private final String usdtSourceAddress;

    public TatumCustodyProvider(
            ObjectMapper objectMapper,
            TatumAddressIndexRepository indexRepository,
            @Value("${oakpay.custody.tatum.api-key:}") String apiKey,
            @Value("${oakpay.custody.tatum.base-url:https://api.tatum.io}") String baseUrl,
            @Value("${oakpay.custody.tatum.webhook-base-url:}") String webhookBaseUrl,
            @Value("${oakpay.custody.tatum.webhook-hmac-secret:}") String webhookHmacSecret,
            @Value("${oakpay.custody.tatum.testnet:true}") boolean testnet,
            @Value("${oakpay.custody.tatum.btc.xpub:}") String btcXpub,
            @Value("${oakpay.custody.tatum.btc.required-confirmations:2}") int btcRequiredConfirmations,
            @Value("${oakpay.custody.tatum.btc.signature-id:}") String btcSignatureId,
            @Value("${oakpay.custody.tatum.btc.source-address:}") String btcSourceAddress,
            @Value("${oakpay.custody.tatum.usdt-tron.xpub:}") String tronXpub,
            @Value("${oakpay.custody.tatum.usdt-tron.required-confirmations:20}") int usdtRequiredConfirmations,
            @Value("${oakpay.custody.tatum.usdt-tron.signature-id:}") String usdtSignatureId,
            @Value("${oakpay.custody.tatum.usdt-tron.source-address:}") String usdtSourceAddress) {
        this.objectMapper = objectMapper;
        this.indexRepository = indexRepository;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.webhookBaseUrl = trimTrailingSlash(webhookBaseUrl);
        this.webhookHmacSecret = webhookHmacSecret == null ? "" : webhookHmacSecret.trim();
        this.testnet = testnet;
        this.btcRequiredConfirmations = Math.max(1, btcRequiredConfirmations);
        this.usdtRequiredConfirmations = Math.max(1, usdtRequiredConfirmations);
        this.btcXpub = trim(btcXpub);
        this.tronXpub = trim(tronXpub);
        this.btcSignatureId = trim(btcSignatureId);
        this.btcSourceAddress = trim(btcSourceAddress);
        this.usdtSignatureId = trim(usdtSignatureId);
        this.usdtSourceAddress = trim(usdtSourceAddress);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String providerName() {
        return "TATUM";
    }

    @Override
    public DepositAddressResult createDepositAddress(DepositAddressRequest request) {
        requireApiKey();
        Asset asset = asset(request.currency(), request.network());
        if (asset.xpub().isBlank()) {
            throw new IllegalStateException("Tatum " + asset.currency() + " " + asset.network() + " xpub is not configured");
        }

        long index = indexRepository.nextIndex();
        String path = asset.currency().equals("BTC")
                ? "/v3/bitcoin/address/" + asset.xpub() + "/" + index
                : "/v3/tron/address/" + asset.xpub() + "/" + index;

        JsonNode response = request("GET", path, "");
        String address = text(response, "address");

        if (!webhookBaseUrl.isBlank()) {
            createAddressAlert(asset, address);
        }

        return new DepositAddressResult("TATUM-ADDRESS:" + asset.currency() + ":" + index, address, null);
    }

    @Override
    public WithdrawalResult submitWithdrawal(WithdrawalRequest request) {
        requireApiKey();
        Asset asset = asset(request.currency(), request.network());

        if (asset.currency().equals("BTC")) {
            if (btcSignatureId.isBlank() || btcSourceAddress.isBlank()) {
                throw new IllegalStateException("Tatum BTC withdrawal requires OAKPAY_TATUM_BTC_SIGNATURE_ID and OAKPAY_TATUM_BTC_SOURCE_ADDRESS");
            }

            Map<String, Object> from = new LinkedHashMap<>();
            from.put("address", btcSourceAddress);
            from.put("signatureId", btcSignatureId);

            Map<String, Object> to = new LinkedHashMap<>();
            to.put("address", request.destinationAddress());
            to.put("value", request.amount());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("fromAddress", java.util.List.of(from));
            body.put("to", java.util.List.of(to));

            JsonNode response = request("POST", "/v3/bitcoin/transaction", toJson(body));
            String reference = firstText(response, "txId", "id", "signatureId");
            if (reference == null) throw new IllegalStateException("Tatum did not return a BTC transaction reference");
            return new WithdrawalResult(reference, ProviderTransactionStatus.SUBMITTED);
        }

        throw new UnsupportedOperationException("Tatum USDT/TRON withdrawals require the Tatum KMS TRC-20 signing flow and are not enabled yet");
    }

    @Override
    public ProviderTransactionStatus getTransactionStatus(String providerReference) {
        requireApiKey();
        for (String candidate : new String[]{"bitcoin-" + networkName(), "tron-" + networkName()}) {
            try {
                JsonNode response = request("GET",
                        "/v4/data/blockchains/transaction?chain=" + candidate + "&hash=" + urlEncode(providerReference), "");
                return statusFrom(response);
            } catch (RuntimeException ignored) {
            }
        }
        return ProviderTransactionStatus.PROCESSING;
    }

    public void enableWebhookHmac() {
        requireApiKey();
        if (webhookHmacSecret.isBlank()) throw new IllegalStateException("OAKPAY_TATUM_WEBHOOK_HMAC_SECRET is not configured");
        request("PUT", "/v4/subscription", toJson(Map.of("hmacSecret", webhookHmacSecret)));
    }

    public JsonNode createAddressAlert(String currency, String network, String address) {
        return createAddressAlert(asset(currency, network), address);
    }

    private JsonNode createAddressAlert(Asset asset, String address) {
        if (webhookBaseUrl.isBlank()) throw new IllegalStateException("OAKPAY_TATUM_WEBHOOK_BASE_URL is not configured");

        String chain = asset.currency().equals("BTC") ? "bitcoin-" + networkName() : "tron-" + networkName();
        Map<String, Object> attr = new LinkedHashMap<>();
        attr.put("address", address);
        attr.put("chain", chain);
        attr.put("url", webhookBaseUrl + "/api/v1/webhooks/tatum/" + asset.currency().toLowerCase(Locale.ROOT));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "ADDRESS_EVENT");
        body.put("attr", attr);

        return request("POST", "/v4/subscription?type=" + networkName(), toJson(body));
    }

    private ProviderTransactionStatus statusFrom(JsonNode response) {
        JsonNode node = response.path("data");
        if (node.isMissingNode() || node.isNull()) node = response;

        String status = firstText(node, "status", "state");
        if (status != null) {
            String normalized = status.toLowerCase(Locale.ROOT);
            if (normalized.contains("fail") || normalized.contains("reject")) return ProviderTransactionStatus.FAILED;
            if (normalized.contains("confirm") || normalized.contains("success") || normalized.equals("complete")) return ProviderTransactionStatus.COMPLETED;
        }
        if (node.has("confirmations") && node.path("confirmations").asInt(0) > 0) return ProviderTransactionStatus.PROCESSING;
        if (node.has("blockNumber") && !node.path("blockNumber").isNull()) return ProviderTransactionStatus.COMPLETED;
        return ProviderTransactionStatus.PROCESSING;
    }

    private Asset asset(String currency, String network) {
        String c = normalize(currency);
        String n = normalize(network);
        if ("BTC".equals(c) && "BITCOIN".equals(n)) return new Asset("BTC", "BITCOIN", btcXpub);
        if ("USDT".equals(c) && "TRON".equals(n)) return new Asset("USDT", "TRON", tronXpub);
        throw new IllegalArgumentException("Tatum custody is not configured for " + c + " on " + n);
    }

    private JsonNode request(String method, String path, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("x-api-key", apiKey);
            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Tatum API request failed with HTTP " + response.statusCode() + ": " + sanitize(response.body()));
            }
            if (response.body() == null || response.body().isBlank()) return objectMapper.createObjectNode();
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Tatum API request interrupted", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException state) throw state;
            throw new IllegalStateException("Tatum API request failed", e);
        }
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) throw new IllegalStateException("OAKPAY_TATUM_API_KEY is not configured");
    }

    private String networkName() { return testnet ? "testnet" : "mainnet"; }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }

    private String trimTrailingSlash(String value) {
        String normalized = value == null || value.isBlank() ? "https://api.tatum.io" : value.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String text(JsonNode node, String field) {
        String value = firstText(node, field);
        if (value == null) throw new IllegalStateException("Tatum response did not contain " + field);
        return value;
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) return value.asText();
        }
        return null;
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new IllegalStateException("Unable to serialize Tatum request", e); }
    }

    private String urlEncode(String value) { return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private String sanitize(String body) {
        if (body == null || body.isBlank()) return "empty response";
        return body.length() > 1000 ? body.substring(0, 1000) : body;
    }

    private record Asset(String currency, String network, String xpub) {}
}
