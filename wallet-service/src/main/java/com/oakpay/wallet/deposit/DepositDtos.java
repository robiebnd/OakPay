package com.oakpay.wallet.deposit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class DepositDtos {
    private DepositDtos() {}

    public record DepositResponse(
            UUID id,
            String currency,
            String network,
            String address,
            String txHash,
            BigDecimal amount,
            int confirmations,
            int requiredConfirmations,
            String status,
            String failureReason,
            LocalDateTime detectedAt,
            LocalDateTime updatedAt) {
        static DepositResponse from(Deposit d) {
            return new DepositResponse(d.getId(), d.getCurrency(), d.getNetwork(), d.getAddress(), d.getTxHash(),
                    d.getAmount(), d.getConfirmations(), d.getRequiredConfirmations(), d.getStatus().name(),
                    d.getFailureReason(), d.getDetectedAt(), d.getUpdatedAt());
        }
    }

    /** Payload accepted only from a trusted blockchain/custody integration. */
    public record BlockchainDepositWebhook(
            @NotBlank String network,
            @NotBlank String address,
            @NotBlank String txHash,
            @NotBlank String currency,
            @NotNull @DecimalMin(value = "0.00000001") BigDecimal amount,
            @Min(0) int confirmations,
            @Min(1) int requiredConfirmations) {}
}
