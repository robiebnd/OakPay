package com.oakpay.wallet.custody;

import java.math.BigDecimal;
import java.util.UUID;

public interface CustodyProvider {

    String providerName();

    DepositAddressResult createDepositAddress(DepositAddressRequest request);

    WithdrawalResult submitWithdrawal(WithdrawalRequest request);

    default ProviderTransactionStatus getTransactionStatus(String providerReference) {
        throw new UnsupportedOperationException("Transaction status lookup is not implemented by " + providerName());
    }

    record DepositAddressRequest(
            UUID userId,
            String currency,
            String network,
            String idempotencyKey) {}

    record DepositAddressResult(
            String providerReference,
            String address,
            String memoTag) {}

    record WithdrawalRequest(
            UUID userId,
            String currency,
            String network,
            BigDecimal amount,
            String destinationAddress,
            String memoTag,
            String idempotencyKey) {}

    record WithdrawalResult(
            String providerReference,
            ProviderTransactionStatus status) {}

    enum ProviderTransactionStatus {
        SUBMITTED,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
