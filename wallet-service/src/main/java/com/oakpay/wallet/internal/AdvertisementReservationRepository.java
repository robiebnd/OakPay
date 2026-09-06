package com.oakpay.wallet.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AdvertisementReservationRepository extends JpaRepository<AdvertisementReservation, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AdvertisementReservation r where r.reference = :reference")
    Optional<AdvertisementReservation> findByReferenceForUpdate(@Param("reference") String reference);
}
