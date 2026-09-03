package com.oakpay.trading.p2p;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface P2PTradeRepository extends JpaRepository<P2PTrade, UUID> {
    List<P2PTrade> findAllByBuyerIdOrSellerIdOrderByCreatedAtDesc(UUID buyerId, UUID sellerId);
    List<P2PTrade> findAllByStatusOrderByCreatedAtAsc(P2PTradeStatus status);

    List<P2PTrade> findAllByStatusAndExpiresAtBefore(P2PTradeStatus status, LocalDateTime cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from P2PTrade t where t.id = :id")
    Optional<P2PTrade> findByIdForUpdate(@Param("id") UUID id);
}
