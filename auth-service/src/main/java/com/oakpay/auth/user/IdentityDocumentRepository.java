package com.oakpay.auth.user;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, UUID> {
    List<IdentityDocument> findByKycProfileIdOrderByCreatedAtDesc(UUID kycProfileId);
}
