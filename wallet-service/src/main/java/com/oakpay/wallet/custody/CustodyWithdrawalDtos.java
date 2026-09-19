package com.oakpay.wallet.custody;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class CustodyWithdrawalDtos {
    private CustodyWithdrawalDtos() {}

    public record WithdrawalRequest(
            @NotBlank String currency,
            @NotBlank String network,
            @NotNull @DecimalMin(value = "0.00000001") BigDecimal amount,
            @NotBlank String destinationAddress,
            String memoTag,
            @NotBlank String idempotencyKey) {}

    public record WithdrawalResponse(
            UUID operationId,
            String provider,
            String providerReference,
            String currency,
            BigDecimal amount,
            String status,
            String destinationAddress,
            String memoTag) {
        static WithdrawalResponse from(CustodyOperation operation) {
            return new WithdrawalResponse(operation.getId(), operation.getProviderName(),
                    operation.getProviderReference(), operation.getCurrency(), operation.getAmount(),
                    operation.getStatus().name(), operation.getDestinationAddress(), operation.getMemoTag());
        }
    }
}
