package com.oakpay.wallet.custody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oakpay.wallet.deposit.DepositDtos;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/tatum")
public class TatumWebhookController {
    private static final Logger log = LoggerFactory.getLogger(TatumWebhookController.class);
    private static final String USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

    private final ObjectMapper objectMapper;
    private final CustodyWebhookEventService eventService;
    private final String hmacSecret;
    private final int btcRequiredConfirmations;
    private final int usdtRequiredConfirmations;

    public TatumWebhookController(
            ObjectMapper objectMapper,
            CustodyWebhookEventService eventService,
            @Value("${oakpay.custody.tatum.webhook-hmac-secret:}") String hmacSecret,
            @Value("${oakpay.custody.tatum.btc.required-confirmations:2}") int btcRequiredConfirmations,
            @Value("${oakpay.custody.tatum.usdt-tron.required-confirmations:20}") int usdtRequiredConfirmations) {
        this.objectMapper = objectMapper;
        this.eventService = eventService;
        this.hmacSecret = hmacSecret == null ? "" : hmacSecret.trim();
        this.btcRequiredConfirmations = Math.max(1, btcRequiredConfirmations);
        this.usdtRequiredConfirmations = Math.max(1, usdtRequiredConfirmations);
    }

    @GetMapping("/diagnostic")
    public Map<String, Object> diagnostic() {
        return Map.of(
                "status", "OK",
                "controller", "TatumWebhookController",
                "timestamp", java.time.Instant.now().toString()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {
        int status = exception.getStatusCode().value();
        String message = exception.getReason() == null
                ? exception.getStatusCode().toString()
                : exception.getReason();

        log.warn("Tatum webhook rejected status={} path={} message={}",
                status, request.getRequestURI(), message);

        return org.springframework.http.ResponseEntity
                .status(exception.getStatusCode())
                .body(Map.of(
                        "timestamp", java.time.Instant.now().toString(),
                        "status", status,
                        "error", exception.getStatusCode().toString(),
                        "message", message,
                        "path", request.getRequestURI()
                ));
    }

    @PostMapping("/{asset}")
    public DepositDtos.DepositResponse receive(
            @PathVariable String asset,
            @RequestHeader(value = "x-payload-hash", required = false) String payloadHash,
            HttpServletRequest request) {
        System.err.println("=== TATUM WEBHOOK CONTROLLER REACHED ===");
        String eventId = "unknown";
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            eventId = sha256(rawBody);
            verify(payloadHash, rawBody);

            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode data = root.has("data") ? root.path("data") : root;
            String normalizedAsset = asset.trim().toUpperCase(Locale.ROOT);

            String address = first(data, "address", "to");
            String txHash = first(data, "txId", "txHash", "hash");
            String chain = first(data, "chain");
            String amountText = first(data, "amount", "value", "amountDecimal");
            String contractAddress = first(data, "contractAddress");
            String symbol = first(data.path("tokenMetadata"), "symbol");

            if (address == null || txHash == null || amountText == null || chain == null) {
                throw new IllegalArgumentException("Tatum webhook is missing required transaction fields");
            }

            if ("USDT".equals(normalizedAsset)) {
                if (!chain.toLowerCase(Locale.ROOT).startsWith("tron")) {
                    throw new IllegalArgumentException("USDT webhook is not a TRON transaction");
                }
                boolean usdt = "USDT_TRON".equalsIgnoreCase(contractAddress)
                        || USDT_CONTRACT.equalsIgnoreCase(contractAddress)
                        || "USDT".equalsIgnoreCase(symbol);
                if (!usdt) throw new IllegalArgumentException("TRON webhook is not a USDT transfer");
            }

            String network = "BTC".equals(normalizedAsset) ? "BITCOIN" : "TRON";
            int required = "BTC".equals(normalizedAsset) ? btcRequiredConfirmations : usdtRequiredConfirmations;
            int confirmations = confirmationCount(data);

            DepositDtos.BlockchainDepositWebhook payload = new DepositDtos.BlockchainDepositWebhook(
                    network, address, txHash, normalizedAsset,
                    new java.math.BigDecimal(amountText), confirmations, required);

            return eventService.processDeposit(
                    "TATUM", eventId, objectMapper.writeValueAsString(payload));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Rejected Tatum webhook eventId={}: {}", eventId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Unable to read Tatum webhook eventId={}", eventId, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read Tatum webhook payload", e);
        } catch (RuntimeException e) {
            log.error("Unexpected Tatum webhook processing failure eventId={}", eventId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Tatum webhook processing failed: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()),
                    e);
        }
    }

    private void verify(String supplied, String rawBody) {
        if (hmacSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tatum webhook HMAC secret is not configured");
        }
        if (supplied == null || supplied.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Missing x-payload-hash");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            String expected = Base64.getEncoder().encodeToString(
                    mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));

            log.debug("Tatum webhook HMAC comparison suppliedHash={} expectedHash={} bodyLength={}",
                    supplied, expected, rawBody.length());

            if (!MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    supplied.getBytes(StandardCharsets.UTF_8))) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid Tatum webhook signature");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Unable to verify Tatum webhook", e);
        }
    }

    private int confirmationCount(JsonNode node) {
        JsonNode value = node.path("confirmations");
        return value.isNumber() ? Math.max(0, value.asInt()) : 0;
    }

    private String first(JsonNode node, String... fields) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash Tatum webhook", e);
        }
    }
}
