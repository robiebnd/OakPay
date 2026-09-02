package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface P2PPaymentAccountRepository extends JpaRepository<P2PPaymentAccount, UUID> {
    List<P2PPaymentAccount> findAllByOwnerIdAndActiveTrueOrderByCreatedAtDesc(UUID ownerId);
}
