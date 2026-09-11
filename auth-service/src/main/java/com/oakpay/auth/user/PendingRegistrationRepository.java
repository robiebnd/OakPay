package com.oakpay.auth.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, UUID> {
    Optional<PendingRegistration> findByEmailIgnoreCase(String email);
    void deleteByEmailIgnoreCase(String email);
}
