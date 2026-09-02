package com.oakpay.trading.p2p;

import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PPaymentAccountDtos {
    private P2PPaymentAccountDtos() {}

    public record CreateRequest(String method, String accountName, String accountIdentifier, String instructions) {}

    public record Response(UUID id, UUID ownerId, String method, String accountName,
                           String accountIdentifier, String instructions, boolean active,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        static Response from(P2PPaymentAccount a) {
            return new Response(a.getId(), a.getOwnerId(), a.getMethod(), a.getAccountName(),
                    a.getAccountIdentifier(), a.getInstructions(), a.isActive(),
                    a.getCreatedAt(), a.getUpdatedAt());
        }
    }
}
