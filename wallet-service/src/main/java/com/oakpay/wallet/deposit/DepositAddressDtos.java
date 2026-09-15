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

    /** Used only by a trusted custody/address provider. */
    public record AssignAddressRequest(
            @NotBlank String userId,
            @NotBlank String currency,
            @NotBlank String network,
            @NotBlank String address,
            String memoTag) {}

    /** Development-only request for the authenticated mobile test-address flow. */
    public record TestAddressRequest(
            @NotBlank String currency,
            @NotBlank String network) {}

    /** Development-only request for generating a non-blockchain test address. */
    public record GenerateTestAddressRequest(
            @NotBlank String userId,
            @NotBlank String currency,
            @NotBlank String network) {}
}
