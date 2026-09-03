package com.oakpay.trading.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportedAssetRepository extends JpaRepository<SupportedAsset, UUID> {
    Optional<SupportedAsset> findBySymbolIgnoreCase(String symbol);
    List<SupportedAsset> findAllByStatusOrderBySymbolAsc(AssetStatus status);
}
