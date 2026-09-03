package com.oakpay.trading.asset;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SupportedAssetService {
    private final SupportedAssetRepository repository;

    public SupportedAssetService(SupportedAssetRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SupportedAssetDtos.AssetResponse> active() {
        return repository.findAllByStatusOrderBySymbolAsc(AssetStatus.ACTIVE).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public SupportedAssetDtos.AssetResponse get(String symbol) {
        return response(find(symbol));
    }

    @Transactional
    public SupportedAssetDtos.AssetResponse create(SupportedAssetDtos.CreateRequest request) {
        String symbol = normalize(request.symbol());
        if (repository.findBySymbolIgnoreCase(symbol).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset already exists");
        }
        validateLimits(request.minTradeAmount(), request.minDepositAmount(), request.minWithdrawalAmount());
        SupportedAsset asset = new SupportedAsset();
        asset.setSymbol(symbol);
        asset.setName(request.name().trim());
        asset.setAssetType(request.assetType().trim().toUpperCase(Locale.ROOT));
        asset.setMinTradeAmount(request.minTradeAmount());
        asset.setMinDepositAmount(request.minDepositAmount());
        asset.setMinWithdrawalAmount(request.minWithdrawalAmount());
        asset.setDecimalPlaces(request.decimalPlaces());
        asset.setP2pEnabled(request.p2pEnabled() == null || request.p2pEnabled());
        asset.setSpotEnabled(request.spotEnabled() != null && request.spotEnabled());
        asset.setStatus(AssetStatus.ACTIVE);
        return response(repository.save(asset));
    }

    @Transactional
    public SupportedAssetDtos.AssetResponse update(String symbol, SupportedAssetDtos.UpdateRequest request) {
        SupportedAsset asset = find(symbol);
        validateLimits(request.minTradeAmount(), request.minDepositAmount(), request.minWithdrawalAmount());
        asset.setName(request.name().trim());
        asset.setMinTradeAmount(request.minTradeAmount());
        asset.setMinDepositAmount(request.minDepositAmount());
        asset.setMinWithdrawalAmount(request.minWithdrawalAmount());
        asset.setDecimalPlaces(request.decimalPlaces());
        asset.setStatus(request.status());
        asset.setP2pEnabled(request.p2pEnabled());
        asset.setSpotEnabled(request.spotEnabled());
        return response(repository.save(asset));
    }

    private SupportedAsset find(String symbol) {
        return repository.findBySymbolIgnoreCase(normalize(symbol))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset symbol is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private void validateLimits(BigDecimal trade, BigDecimal deposit, BigDecimal withdrawal) {
        if (trade.signum() <= 0 || deposit.signum() <= 0 || withdrawal.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset limits must be positive");
        }
    }

    private SupportedAssetDtos.AssetResponse response(SupportedAsset a) {
        return new SupportedAssetDtos.AssetResponse(a.getId(), a.getSymbol(), a.getName(), a.getAssetType(),
                a.getMinTradeAmount(), a.getMinDepositAmount(), a.getMinWithdrawalAmount(), a.getDecimalPlaces(),
                a.getStatus(), a.getP2pEnabled(), a.getSpotEnabled());
    }
}
