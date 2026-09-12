package com.oakpay.auth.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycProfileRepository extends JpaRepository<KycProfile, UUID> {
    Optional<KycProfile> findByUserId(UUID userId);
    List<KycProfile> findByStatusOrderBySubmittedAtAsc(String status);
    long countByStatus(String status);
}