package com.oakpay.wallet.custody;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CustodyWebhookEventRepository extends JpaRepository<CustodyWebhookEvent, UUID> {
    Optional<CustodyWebhookEvent> findByProviderNameAndEventId(String providerName, String eventId);
}
