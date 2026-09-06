package com.oakpay.wallet.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementReservationOperationRepository extends JpaRepository<AdvertisementReservationOperation, UUID> {
    Optional<AdvertisementReservationOperation> findByReservationReferenceAndOperationReference(String reservationReference, String operationReference);
}
