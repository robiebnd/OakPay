package com.oakpay.wallet.deposit;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public final class DepositAddressDtos {
    private DepositAddressDtos() {}

    public record DepositAddressResponse(
            UUID id,
            String currency,
            String network,
            String address,
            String memoTag,
            String status,
            LocalDateTime createdAt) {
        static DepositAddressResponse from(DepositAddress address) {
            return new DepositAddressResponse(address.getId(), address.getCurrency(), address.getNetwork(),
                    address.getAddress(), address.getMemoTag(), address.getStatus().name(), address.getCreatedAt());
        }
    }

    /** Used only by a trusted custody/address provider. Never generate fake blockchain addresses here. */
    public record AssignAddressRequest(
            @NotBlank String userId,
            @NotBlank String currency,
            @NotBlank String network,
            @NotBlank String address,
            String memoTag) {}
}
