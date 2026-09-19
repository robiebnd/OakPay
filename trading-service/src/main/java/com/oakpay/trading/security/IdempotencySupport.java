package com.oakpay.trading.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class IdempotencySupport {
    private IdempotencySupport() {}

    public static String normalizeKey(String value) {
        if (value == null || value.isBlank()) return null;
        String key = value.trim();
        if (key.length() > 100) throw new IllegalArgumentException("Idempotency-Key must not exceed 100 characters");
        return key;
    }

    public static String hash(String value) {
        if (value == null) return null;
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static UUID deterministicId(UUID userId, String operation, String key) {
        String input = userId + ":" + operation + ":" + key;
        return UUID.nameUUIDFromBytes(input.getBytes(StandardCharsets.UTF_8));
    }
}
