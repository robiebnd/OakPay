package com.oakpay.wallet.api;

import com.oakpay.wallet.ledger.LedgerBalanceType;
import com.oakpay.wallet.ledger.LedgerDirection;
import com.oakpay.wallet.ledger.LedgerEntry;
import com.oakpay.wallet.ledger.LedgerStatus;
import com.oakpay.wallet.ledger.LedgerTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class LedgerDtos {
    private LedgerDtos() {}

    public record BalanceRequest(
            @NotNull @DecimalMin(value = "0.000000000000000001") BigDecimal amount,
            @NotBlank String reference,
            String metadata) {}

    public record LedgerResponse(
            UUID id,
            UUID walletId,
            UUID userId,
            LedgerTransactionType transactionType,
            LedgerStatus status,
            LedgerDirection direction,
            LedgerBalanceType balanceType,
            String currency,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String reference,
            String metadata,
            LocalDateTime createdAt) {
        public static LedgerResponse from(LedgerEntry e) {
            return new LedgerResponse(e.getId(), e.getWalletId(), e.getUserId(), e.getTransactionType(),
                    e.getStatus(), e.getDirection(), e.getBalanceType(), e.getCurrency(), e.getAmount(),
                    e.getBalanceBefore(), e.getBalanceAfter(), e.getReference(), e.getMetadata(), e.getCreatedAt());
        }
    }
}
