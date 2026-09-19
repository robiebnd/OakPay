package com.oakpay.trading.p2p;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface P2PCommissionRepository extends JpaRepository<P2PCommission, UUID> {
    Optional<P2PCommission> findByTradeId(UUID tradeId);
    List<P2PCommission> findAllByStatusOrderByCreatedAtDesc(P2PCommissionStatus status, Pageable pageable);
}
