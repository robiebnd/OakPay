package com.oakpay.wallet.custody;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface CustodyWebhookEventRepository extends JpaRepository<CustodyWebhookEvent, UUID> {
    Optional<CustodyWebhookEvent> findByProviderNameAndEventId(String providerName, String eventId);

    @Modifying
    @Query(value = """
            INSERT INTO custody_webhook_events
                (id,provider_name,event_id,payload_hash,status,deposit_id,created_at,updated_at)
            VALUES
                (:id,:providerName,:eventId,:payloadHash,'PROCESSING',NULL,:createdAt,:updatedAt)
            ON CONFLICT (provider_name,event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("providerName") String providerName,
                       @Param("eventId") String eventId,
                       @Param("payloadHash") String payloadHash,
                       @Param("createdAt") java.time.LocalDateTime createdAt,
                       @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
