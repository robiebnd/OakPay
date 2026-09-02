package com.oakpay.trading.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findAllByBuyerIdOrSellerIdOrderByCreatedAtDesc(UUID buyerId, UUID sellerId);
}
