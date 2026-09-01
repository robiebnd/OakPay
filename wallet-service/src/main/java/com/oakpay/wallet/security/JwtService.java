package com.oakpay.wallet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey signingKey;

    public JwtService(@Value("${oakpay.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("oakpay.jwt.secret must be at least 32 bytes");
        this.signingKey = Keys.hmacShaKeyFor(bytes);
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public UUID userId(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Access token required");
        }
        return UUID.fromString(claims.get("uid", String.class));
    }
}
