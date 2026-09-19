package com.oakpay.wallet.custody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Component
public class HmacCustodyWebhookVerifier implements CustodyWebhookVerifier {
    private final String secret;
    private final long toleranceSeconds;

    public HmacCustodyWebhookVerifier(
            @Value("${oakpay.custody.webhook-secret:}") String secret,
            @Value("${oakpay.custody.webhook-tolerance-seconds:300}") long toleranceSeconds) {
        this.secret = secret;
        this.toleranceSeconds = toleranceSeconds;
    }

    @Override
    public void verify(String provider, String eventId, String timestamp, String signature, String rawBody) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Custody webhook secret is not configured");
        }
        if (provider == null || provider.isBlank() || eventId == null || eventId.isBlank()
                || timestamp == null || timestamp.isBlank() || signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Missing custody webhook authentication headers");
        }

        long epoch;
        try {
            epoch = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid webhook timestamp");
        }

        long age = Math.abs(Instant.now().getEpochSecond() - epoch);
        if (age > toleranceSeconds) {
            throw new IllegalArgumentException("Expired custody webhook");
        }

        String signedPayload = timestamp + "." + eventId + "." + rawBody;
        String expected = hmacSha256Hex(secret, signedPayload);
        String supplied = signature.startsWith("sha256=") ? signature.substring(7) : signature;

        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                supplied.toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid custody webhook signature");
        }
    }

    private String hmacSha256Hex(String secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to verify custody webhook", e);
        }
    }
}
