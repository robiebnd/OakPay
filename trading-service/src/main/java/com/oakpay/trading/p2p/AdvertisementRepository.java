package com.oakpay.trading.p2p;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    List<Advertisement> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    List<Advertisement> findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceAscCreatedAtAsc(
            OrderSide side, String asset, String fiatCurrency, AdStatus status, Pageable pageable);

    List<Advertisement> findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceDescCreatedAtAsc(
            OrderSide side, String asset, String fiatCurrency, AdStatus status, Pageable pageable);

    @Query("select coalesce(sum(a.availableQuantity), 0) from Advertisement a " +
           "where a.ownerId = :ownerId and a.side = :side and a.asset = :asset " +
           "and a.status in :statuses")
    BigDecimal sumAvailableQuantityByOwnerAndSideAndAssetAndStatuses(
            @Param("ownerId") UUID ownerId,
            @Param("side") OrderSide side,
            @Param("asset") String asset,
            @Param("statuses") List<AdStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Advertisement a where a.id = :id")
    Optional<Advertisement> findByIdForUpdate(@Param("id") UUID id);
}
