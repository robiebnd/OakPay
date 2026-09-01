package com.oakpay.auth.service;

import com.oakpay.auth.api.AuthDtos;
import com.oakpay.auth.security.JwtService;
import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String REFRESH_KEY_PREFIX = "oakpay:auth:refresh:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final Duration refreshTokenTtl;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       StringRedisTemplate redisTemplate,
                       @Value("${oakpay.jwt.refresh-token-ttl:7d}") Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public AuthDtos.UserResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user = userRepository.save(user);

        return toResponse(user);
    }

    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return issueTokens(principal);
    }

    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request) {
        Claims claims = jwtService.parse(request.refreshToken());
        if (!jwtService.isRefreshToken(claims)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String tokenHash = sha256(request.refreshToken());
        String userId = redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + tokenHash);
        if (userId == null) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserPrincipal principal = UserPrincipal.from(user);

        redisTemplate.delete(REFRESH_KEY_PREFIX + tokenHash);
        return issueTokens(principal);
    }

    public void logout(AuthDtos.RefreshRequest request) {
        String tokenHash = sha256(request.refreshToken());
        redisTemplate.delete(REFRESH_KEY_PREFIX + tokenHash);
    }

    private AuthDtos.TokenResponse issueTokens(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);
        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + sha256(refreshToken),
                principal.getUserId().toString(),
                refreshTokenTtl);
        return new AuthDtos.TokenResponse("Bearer", accessToken, refreshToken, jwtService.getAccessTokenTtlSeconds());
    }

    private AuthDtos.UserResponse toResponse(User user) {
        return new AuthDtos.UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.isEmailVerified());
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
