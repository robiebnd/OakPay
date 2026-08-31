package com.oakpay.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final Key signingKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(
            @Value("${oakpay.jwt.secret}") String secret,
            @Value("${oakpay.jwt.access-token-ttl:15m}") Duration accessTokenTtl,
            @Value("${oakpay.jwt.refresh-token-ttl:7d}") Duration refreshTokenTtl) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("oakpay.jwt.secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String generateAccessToken(UserPrincipal principal) {
        return generate(principal.getUserId(), principal.getUsername(), accessTokenTtl, "access");
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return generate(principal.getUserId(), principal.getUsername(), refreshTokenTtl, "refresh");
    }

    private String generate(UUID userId, String subject, Duration ttl, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .subject(subject)
                .claim("uid", userId.toString())
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) signingKey).build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }
}
