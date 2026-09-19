package com.oakpay.wallet.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WalletOperationRepository extends JpaRepository<WalletOperation, UUID> {
    Optional<WalletOperation> findByOperationTypeAndReference(String operationType, String reference);
}
