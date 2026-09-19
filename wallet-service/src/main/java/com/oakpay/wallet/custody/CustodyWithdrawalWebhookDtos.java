package com.oakpay.wallet.custody;

import jakarta.validation.constraints.NotBlank;

public final class CustodyWithdrawalWebhookDtos {
    private CustodyWithdrawalWebhookDtos() {}

    public record Webhook(
            @NotBlank String providerReference,
            @NotBlank String status) {}
}
