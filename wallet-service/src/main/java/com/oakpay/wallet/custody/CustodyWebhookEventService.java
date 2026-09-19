package com.oakpay.wallet.custody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oakpay.wallet.deposit.DepositDtos;
import com.oakpay.wallet.deposit.DepositService;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class CustodyWebhookEventService {
    private final CustodyWebhookEventRepository eventRepository;
    private final DepositService depositService;
    private final CustodyWithdrawalService withdrawalService;
    private final CustodyOperationRepository operationRepository;
    private final ObjectMapper objectMapper;
    private final CustodyMetrics metrics;

    public CustodyWebhookEventService(CustodyWebhookEventRepository eventRepository,
                                      DepositService depositService,
                                      CustodyWithdrawalService withdrawalService,
                                      CustodyOperationRepository operationRepository,
                                      ObjectMapper objectMapper,
                                      CustodyMetrics metrics) {
        this.eventRepository = eventRepository;
        this.depositService = depositService;
        this.withdrawalService = withdrawalService;
        this.operationRepository = operationRepository;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @Transactional
    public DepositDtos.DepositResponse processDeposit(String provider, String eventId, String rawBody) {
        String normalizedProvider = normalize(provider);
        String normalizedEventId = normalize(eventId);
        String payloadHash = sha256(rawBody);

        UUID eventUuid = UUID.randomUUID();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int inserted = eventRepository.insertIfAbsent(eventUuid, normalizedProvider, normalizedEventId,
                payloadHash, now, now);

        CustodyWebhookEvent event = eventRepository
                .findByProviderNameAndEventId(normalizedProvider, normalizedEventId)
                .orElseThrow(() -> new IllegalStateException("Webhook event could not be loaded"));

        if (inserted == 0) {
            if (!event.getPayloadHash().equals(payloadHash)) {
                throw new IllegalArgumentException("Webhook event ID was already used with a different payload");
            }
            if (event.getDepositId() == null) {
                throw new IllegalStateException("Webhook event is still being processed");
            }
            return depositService.getById(event.getDepositId());
        }

        try {
            DepositDtos.BlockchainDepositWebhook payload =
                    objectMapper.readValue(rawBody, DepositDtos.BlockchainDepositWebhook.class);
            DepositDtos.DepositResponse result = depositService.processWebhook(payload);
            event.setDepositId(result.id());
            event.setStatus(CustodyWebhookEventStatus.PROCESSED);
            eventRepository.save(event);
            metrics.webhookProcessed();
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid custody deposit webhook", e);
        }
    }

    @Transactional
    public CustodyWithdrawalDtos.WithdrawalResponse processWithdrawal(String provider, String eventId, String rawBody) {
        String normalizedProvider = normalize(provider);
        String normalizedEventId = normalize(eventId);
        String payloadHash = sha256(rawBody);

        UUID eventUuid = UUID.randomUUID();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int inserted = eventRepository.insertIfAbsent(eventUuid, normalizedProvider, normalizedEventId,
                payloadHash, now, now);

        CustodyWebhookEvent event = eventRepository
                .findByProviderNameAndEventId(normalizedProvider, normalizedEventId)
                .orElseThrow(() -> new IllegalStateException("Webhook event could not be loaded"));

        if (inserted == 0) {
            if (!event.getPayloadHash().equals(payloadHash)) {
                throw new IllegalArgumentException("Webhook event ID was already used with a different payload");
            }
            if (event.getWithdrawalOperationId() == null) {
                throw new IllegalStateException("Webhook event is still being processed");
            }
            return withdrawalResponse(event.getWithdrawalOperationId());
        }

        try {
            CustodyWithdrawalWebhookDtos.Webhook payload =
                    objectMapper.readValue(rawBody, CustodyWithdrawalWebhookDtos.Webhook.class);
            CustodyProvider.ProviderTransactionStatus status =
                    CustodyProvider.ProviderTransactionStatus.valueOf(payload.status().trim().toUpperCase(Locale.ROOT));
            CustodyOperation operation = withdrawalService.applyProviderStatus(
                    normalizedProvider, payload.providerReference().trim(), status);
            event.setWithdrawalOperationId(operation.getId());
            event.setStatus(CustodyWebhookEventStatus.PROCESSED);
            eventRepository.save(event);
            metrics.webhookProcessed();
            return CustodyWithdrawalDtos.WithdrawalResponse.from(operation);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid custody withdrawal webhook", e);
        }
    }

    private CustodyWithdrawalDtos.WithdrawalResponse withdrawalResponse(UUID operationId) {
        CustodyOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalStateException("Withdrawal custody operation not found"));
        return CustodyWithdrawalDtos.WithdrawalResponse.from(operation);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase();
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash webhook payload", e);
        }
    }
}
