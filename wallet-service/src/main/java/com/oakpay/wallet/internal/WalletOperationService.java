package com.oakpay.wallet.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletOperationService {
    private final WalletOperationRepository repository;

    public WalletOperationService(WalletOperationRepository repository) { this.repository = repository; }

    @Transactional
    public boolean begin(String type, String reference, UUID userId, String currency, java.math.BigDecimal amount, String fingerprint) {
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("Reference is required");
        String operationType = type.trim().toUpperCase();
        String requestHash = hash(fingerprint);
        var existing = repository.findByOperationTypeAndReference(operationType, reference.trim());
        if (existing.isPresent()) {
            WalletOperation op = existing.get();
            if (!op.getUserId().equals(userId)
                    || !op.getCurrency().equalsIgnoreCase(currency)
                    || op.getAmount().compareTo(amount) != 0
                    || !op.getRequestHash().equals(requestHash)) {
                throw new IllegalStateException("Reference was already used for a different wallet operation");
            }
            return false;
        }

        WalletOperation op = new WalletOperation();
        op.setOperationType(operationType);
        op.setReference(reference.trim());
        op.setRequestHash(requestHash);
        op.setUserId(userId);
        op.setCurrency(currency.trim().toUpperCase());
        op.setAmount(amount);
        try {
            repository.saveAndFlush(op);
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException("Wallet operation was concurrently submitted; retry with the same reference");
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
