package com.oakpay.wallet.custody;

public interface CustodyWebhookVerifier {
    void verify(String provider, String eventId, String timestamp, String signature, String rawBody);
}
