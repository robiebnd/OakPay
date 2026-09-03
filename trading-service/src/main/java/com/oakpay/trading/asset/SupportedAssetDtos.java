package com.oakpay.trading.asset;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public final class SupportedAssetDtos {
    private SupportedAssetDtos() {}

    public record AssetResponse(
            UUID id,
            String symbol,
            String name,
            String assetType,
            BigDecimal minTradeAmount,
            BigDecimal minDepositAmount,
            BigDecimal minWithdrawalAmount,
            Integer decimalPlaces,
            AssetStatus status,
            Boolean p2pEnabled,
            Boolean spotEnabled
    ) {}

    public record CreateRequest(
            @NotBlank @Size(max = 20) String symbol,
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 20) String assetType,
            @NotNull @DecimalMin("0.000000000000000001") BigDecimal minTradeAmount,
            @NotNull @DecimalMin("0.000000000000000001") BigDecimal minDepositAmount,
            @NotNull @DecimalMin("0.000000000000000001") BigDecimal minWithdrawalAmount,
            @NotNull @Min(0) @Max(18) Integer decimalPlaces,
            Boolean p2pEnabled,
            Boolean spotEnabled
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @DecimalMin("0.000000000000000001") BigDecimal minTradeAmount,
            @NotNull @DecimalMin("0.000000000000000001") BigDecimal minDepositAmount,
            @NotNull @DecimalMin("0.000000000000000001") BigDecimal minWithdrawalAmount,
            @NotNull @Min(0) @Max(18) Integer decimalPlaces,
            @NotNull AssetStatus status,
            @NotNull Boolean p2pEnabled,
            @NotNull Boolean spotEnabled
    ) {}
}
