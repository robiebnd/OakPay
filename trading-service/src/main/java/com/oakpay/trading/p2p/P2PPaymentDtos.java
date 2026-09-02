package com.oakpay.trading.p2p;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PPaymentDtos {
    private P2PPaymentDtos() {}

    public record SubmitRequest(String paymentReference, String note) {}

    public record PaymentResponse(
            UUID id,
            UUID tradeId,
            UUID payerId,
            UUID payeeId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String paymentReference,
            String note,
            PaymentStatus status,
            LocalDateTime submittedAt,
            LocalDateTime verifiedAt,
            LocalDateTime createdAt
    ) {
        static PaymentResponse from(P2PPayment p) {
            return new PaymentResponse(p.getId(), p.getTradeId(), p.getPayerId(), p.getPayeeId(),
                    p.getAmount(), p.getCurrency(), p.getPaymentMethod(), p.getPaymentReference(),
                    p.getNote(), p.getStatus(), p.getSubmittedAt(), p.getVerifiedAt(), p.getCreatedAt());
        }
    }
}
