package com.oakpay.wallet.custody;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CustodyProviderService {
    private final ObjectProvider<CustodyProvider> providerProvider;
    private final CustodyOperationRepository operationRepository;
    private final CustodyOperationStateService operationStateService;

    public CustodyProviderService(ObjectProvider<CustodyProvider> providerProvider,
                                  CustodyOperationRepository operationRepository,
                                  CustodyOperationStateService operationStateService) {
        this.providerProvider = providerProvider;
        this.operationRepository = operationRepository;
        this.operationStateService = operationStateService;
    }

    @Transactional
    public CustodyOperation requestDepositAddress(UUID userId, String currency, String network, String idempotencyKey) {
        String key = normalizeKey(idempotencyKey);
        CustodyOperation existing = operationRepository
                .findByOperationTypeAndIdempotencyKey(CustodyOperationType.DEPOSIT_ADDRESS, key)
                .orElse(null);
        if (existing != null) return existing;

        CustodyProvider provider = requireProvider();

        CustodyOperation operation = new CustodyOperation();
        operation.setOperationType(CustodyOperationType.DEPOSIT_ADDRESS);
        operation.setIdempotencyKey(key);
        operation.setProviderName(provider.providerName());
        operation.setUserId(userId);
        operation.setCurrency(normalize(currency));
        operation.setNetwork(normalize(network));
        operation.setStatus(CustodyOperationStatus.REQUESTED);
        operationRepository.saveAndFlush(operation);

        CustodyProvider.DepositAddressResult result = provider.createDepositAddress(
                new CustodyProvider.DepositAddressRequest(userId, normalize(currency), normalize(network), key));

        operation.setProviderReference(requireReference(result.providerReference()));
        operation.setProviderAddress(requireAddress(result.address()));
        operation.setMemoTag(result.memoTag());
        operation.setStatus(CustodyOperationStatus.SUBMITTED);
        return operationRepository.save(operation);
    }

    @Transactional
    public CustodyOperation submitWithdrawal(UUID userId, String currency, String network,
                                              BigDecimal amount, String destinationAddress,
                                              String memoTag, String idempotencyKey) {
        String key = normalizeKey(idempotencyKey);
        CustodyOperation existing = operationRepository
                .findByOperationTypeAndIdempotencyKey(CustodyOperationType.WITHDRAWAL, key)
                .orElse(null);
        if (existing != null) return existing;

        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        if (destinationAddress == null || destinationAddress.isBlank()) throw new IllegalArgumentException("Destination address is required");

        CustodyProvider provider = requireProvider();

        CustodyOperation operation = new CustodyOperation();
        operation.setOperationType(CustodyOperationType.WITHDRAWAL);
        operation.setIdempotencyKey(key);
        operation.setProviderName(provider.providerName());
        operation.setUserId(userId);
        operation.setCurrency(normalize(currency));
        operation.setNetwork(normalize(network));
        operation.setAmount(amount);
        operation.setDestinationAddress(destinationAddress.trim());
        operation.setMemoTag(memoTag);
        operation.setStatus(CustodyOperationStatus.REQUESTED);
        operationRepository.saveAndFlush(operation);

        try {
            CustodyProvider.WithdrawalResult result = provider.submitWithdrawal(
                    new CustodyProvider.WithdrawalRequest(userId, normalize(currency), normalize(network),
                            amount, destinationAddress.trim(), memoTag, key));

            operation.setProviderReference(requireReference(result.providerReference()));
            operation.setStatus(mapStatus(result.status()));
            return operationRepository.save(operation);
        } catch (RuntimeException e) {
            // A timeout/network error does not prove that the provider rejected the withdrawal.
            // Keep the funds locked and persist an explicit recovery state in its own transaction.
            operationStateService.markSubmissionUnknown(operation);
            throw new CustodySubmissionUnknownException(
                    "Custody provider submission outcome is unknown; reconciliation is required", e);
        }
    }

    @Transactional(readOnly = true)
    public DepositStatusResult getDepositTransactionStatus(String providerName, String currency, String network, String transactionHash) {
        CustodyProvider provider = requireProvider();
        String normalizedProvider = normalize(providerName);
        if (!provider.providerName().equalsIgnoreCase(normalizedProvider)) {
            throw new IllegalArgumentException("Custody provider is not configured for " + normalizedProvider);
        }
        CustodyProvider.DepositTransactionStatus result = provider.getDepositTransactionStatus(
                normalize(currency), normalize(network), requireReference(transactionHash));
        return new DepositStatusResult(result.status(), result.confirmations());
    }

    public record DepositStatusResult(CustodyProvider.ProviderTransactionStatus status, int confirmations) {}

    @Transactional(readOnly = true)
    public CustodyOperation findWithdrawalByIdempotencyKeyOrNull(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        return operationRepository
                .findByOperationTypeAndIdempotencyKey(CustodyOperationType.WITHDRAWAL, idempotencyKey.trim())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public CustodyOperation findWithdrawalByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        return operationRepository
                .findByOperationTypeAndIdempotencyKey(CustodyOperationType.WITHDRAWAL, idempotencyKey.trim())
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal custody operation not found"));
    }

    @Transactional(readOnly = true)
    public CustodyOperation getByProviderReference(String providerName, String providerReference) {
        return operationRepository.findByProviderNameAndProviderReference(providerName, providerReference)
                .orElseThrow(() -> new IllegalArgumentException("Custody operation not found"));
    }

    @Transactional(readOnly = true)
    public CustodyProvider.ProviderTransactionStatus getTransactionStatus(String providerName,
                                                                           String providerReference) {
        CustodyProvider provider = requireProvider();
        String normalizedProvider = normalize(providerName);
        String normalizedReference = requireReference(providerReference);

        if (!provider.providerName().equalsIgnoreCase(normalizedProvider)) {
            throw new IllegalArgumentException("Custody provider is not configured for " + normalizedProvider);
        }

        return provider.getTransactionStatus(normalizedReference);
    }

    private CustodyProvider requireProvider() {
        CustodyProvider provider = providerProvider.getIfAvailable();
        if (provider == null) {
            throw new IllegalStateException("No custody provider is configured");
        }
        return provider;
    }

    private CustodyOperationStatus mapStatus(CustodyProvider.ProviderTransactionStatus status) {
        return switch (status) {
            case SUBMITTED -> CustodyOperationStatus.SUBMITTED;
            case PROCESSING -> CustodyOperationStatus.PROCESSING;
            case COMPLETED -> CustodyOperationStatus.COMPLETED;
            case FAILED -> CustodyOperationStatus.FAILED;
        };
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase();
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Idempotency key is required");
        return value.trim();
    }

    private String requireAddress(String value) {
        if (value == null || value.isBlank()) throw new IllegalStateException("Custody provider returned no deposit address");
        return value.trim();
    }

    private String requireReference(String value) {
        if (value == null || value.isBlank()) throw new IllegalStateException("Custody provider returned no reference");
        return value.trim();
    }
}
