package com.oakpay.wallet.custody;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustodyOperationRepository extends JpaRepository<CustodyOperation, UUID> {
    Optional<CustodyOperation> findByOperationTypeAndIdempotencyKey(
            CustodyOperationType operationType, String idempotencyKey);

    Optional<CustodyOperation> findByProviderNameAndProviderReference(
            String providerName, String providerReference);

    long countByOperationTypeAndStatusIn(
            CustodyOperationType operationType,
            Collection<CustodyOperationStatus> statuses);

    List<CustodyOperation> findTop100ByOperationTypeAndStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            CustodyOperationType operationType,
            Collection<CustodyOperationStatus> statuses,
            LocalDateTime cutoff);
}
