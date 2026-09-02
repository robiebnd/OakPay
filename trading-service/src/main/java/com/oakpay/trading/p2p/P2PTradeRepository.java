package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface P2PTradeRepository extends JpaRepository<P2PTrade, UUID> {
    List<P2PTrade> findAllByBuyerIdOrSellerIdOrderByCreatedAtDesc(UUID buyerId, UUID sellerId);
    List<P2PTrade> findAllByStatusOrderByCreatedAtAsc(P2PTradeStatus status);
}
