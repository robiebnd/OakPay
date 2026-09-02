package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface P2PDisputeAuditRepository extends JpaRepository<P2PDisputeAudit, UUID> {
    List<P2PDisputeAudit> findAllByDisputeIdOrderByCreatedAtAsc(UUID disputeId);
}
