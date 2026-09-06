package com.oakpay.trading.p2p;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface P2PPaymentRepository extends JpaRepository<P2PPayment, UUID> {
    Optional<P2PPayment> findByTradeId(UUID tradeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from P2PPayment p where p.tradeId = :tradeId")
    Optional<P2PPayment> findByTradeIdForUpdate(@Param("tradeId") UUID tradeId);
}
