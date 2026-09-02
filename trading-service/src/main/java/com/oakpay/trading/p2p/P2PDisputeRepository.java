package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface P2PDisputeRepository extends JpaRepository<P2PDispute, UUID> {
    List<P2PDispute> findAllByStatusOrderByCreatedAtAsc(DisputeStatus status);
    List<P2PDispute> findAllByOpenedByOrderByCreatedAtDesc(UUID openedBy);
}
