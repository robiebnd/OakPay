package com.oakpay.auth.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycProfileRepository extends JpaRepository<KycProfile, UUID> {
    Optional<KycProfile> findByUserId(UUID userId);
}
