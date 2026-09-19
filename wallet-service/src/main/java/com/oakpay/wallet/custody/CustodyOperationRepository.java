package com.oakpay.wallet.custody;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustodyOperationRepository extends JpaRepository<CustodyOperation, UUID> {
    Optional<CustodyOperation> findByOperationTypeAndIdempotencyKey(
            CustodyOperationType operationType, String idempotencyKey);

    Optional<CustodyOperation> findByProviderNameAndProviderReference(
            String providerName, String providerReference);
}
