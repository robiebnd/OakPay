package com.oakpay.wallet.custody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oakpay.wallet.deposit.DepositDtos;
import com.oakpay.wallet.deposit.DepositService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Service
public class BitGoWebhookService {
    private final BitGoCustodyProvider bitGo;
    private final DepositService depositService;
    private final CustodyWebhookEventService eventService;
    private final ObjectMapper objectMapper;
    private final int btcRequiredConfirmations;
    private final int usdtTronRequiredConfirmations;

    public BitGoWebhookService(
            BitGoCustodyProvider bitGo,
            DepositService depositService,
            CustodyWebhookEventService eventService,
            ObjectMapper objectMapper,
            @Value("${oakpay.custody.bitgo.btc.required-confirmations:3}") int btcRequiredConfirmations,
            @Value("${oakpay.custody.bitgo.usdt-tron.required-confirmations:20}") int usdtTronRequiredConfirmations) {
        this.bitGo = bitGo;
        this.depositService = depositService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.btcRequiredConfirmations = positiveConfirmations(btcRequiredConfirmations);
        this.usdtTronRequiredConfirmations = positiveConfirmations(usdtTronRequiredConfirmations);
    }

    @Transactional
    public DepositDtos.DepositResponse processTransferNotification(
            String coinFromPath, String signature, String rawBody) {

        JsonNode notification = readJson(rawBody);
        String webhookId = text(notification, "webhook");
        String eventId = firstText(notification, "id", "idempotencyKey");
        String transferId = text(notification, "transfer");
        String coin = text(notification, "coin");

        if (eventId == null) {
            throw new IllegalArgumentException("BitGo webhook notification has no event ID");
        }
        if (!normalizeCoin(coin).equals(normalizeCoin(coinFromPath))) {
            throw new IllegalArgumentException("BitGo webhook coin does not match request path");
        }

        bitGo.verifyWebhook(webhookId, signature, rawBody);

        String walletId = text(notification, "wallet");
        JsonNode transfer = bitGo.getTransfer(coin, walletId, transferId);

        String type = text(transfer, "type");
        if (!"receive".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("BitGo transfer is not a deposit");
        }

        String state = text(transfer, "state");
        if ("failed".equalsIgnoreCase(state)
                || "rejected".equalsIgnoreCase(state)
                || "removed".equalsIgnoreCase(state)
                || "replaced".equalsIgnoreCase(state)) {
            throw new IllegalArgumentException("BitGo transfer is not a valid deposit state: " + state);
        }

        JsonNode entry = findDepositEntry(transfer);
        String address = text(entry, "address");
        String valueString = firstText(entry, "valueString", "value");
        if (valueString == null) {
            throw new IllegalArgumentException("BitGo transfer entry has no value");
        }

        String currency;
        String network;
        int decimals;
        int requiredConfirmations;

        if ("btc".equalsIgnoreCase(coin)) {
            currency = "BTC";
            network = "BITCOIN";
            decimals = 8;
            requiredConfirmations = btcRequiredConfirmations;
        } else if (normalizeCoin(coin).contains("usdt")
                && normalizeCoin(coin).contains("trx")) {
            currency = "USDT";
            network = "TRON";
            decimals = 6;
            requiredConfirmations = usdtTronRequiredConfirmations;
        } else {
            throw new IllegalArgumentException("Unsupported BitGo deposit coin: " + coin);
        }

        BigDecimal amount = new BigDecimal(valueString)
                .movePointLeft(decimals)
                .setScale(decimals, RoundingMode.UNNECESSARY);

        int confirmations = Math.max(0, transfer.path("confirmations").asInt(0));
        String txHash = text(transfer, "txid");

        DepositDtos.BlockchainDepositWebhook normalizedPayload =
                new DepositDtos.BlockchainDepositWebhook(
                        network, address, txHash, currency, amount,
                        confirmations, requiredConfirmations);
        try {
            String normalizedBody = objectMapper.writeValueAsString(normalizedPayload);
            return eventService.processDeposit("BITGO", eventId, normalizedBody);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to normalize BitGo deposit webhook", e);
        }
    }

    private JsonNode findDepositEntry(JsonNode transfer) {
        JsonNode entries = transfer.path("entries");
        if (!entries.isArray()) throw new IllegalArgumentException("BitGo transfer contains no entries");

        for (JsonNode entry : entries) {
            String address = firstText(entry, "address");
            String value = firstText(entry, "valueString", "value");
            if (address != null && value != null) {
                try {
                    if (new BigDecimal(value).signum() > 0) return entry;
                } catch (NumberFormatException ignored) {
                    // Try the next entry.
                }
            }
        }
        throw new IllegalArgumentException("BitGo receive transfer contains no positive deposit entry");
    }

    private JsonNode readJson(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid BitGo webhook JSON", e);
        }
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null) return value;
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) return null;
        return value.asText().trim();
    }

    private String normalizeCoin(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private int positiveConfirmations(int value) {
        if (value < 1) throw new IllegalArgumentException("Required confirmations must be at least 1");
        return value;
    }
}
