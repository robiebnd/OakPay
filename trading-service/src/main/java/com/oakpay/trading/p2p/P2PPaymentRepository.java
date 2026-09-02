package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface P2PPaymentRepository extends JpaRepository<P2PPayment, UUID> {
    Optional<P2PPayment> findByTradeId(UUID tradeId);
}
