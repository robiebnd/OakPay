package com.oakpay.auth.admin;

import com.oakpay.auth.user.KycProfile;
import com.oakpay.auth.user.KycProfileRepository;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
public class InternalUserStatusController {
    private final UserRepository userRepository;
    private final KycProfileRepository kycProfileRepository;
    private final String internalSecret;

    public InternalUserStatusController(
            UserRepository userRepository,
            KycProfileRepository kycProfileRepository,
            @Value("${oakpay.internal-secret:oakpay-internal-development-secret-change-before-production}") String internalSecret) {
        this.userRepository = userRepository;
        this.kycProfileRepository = kycProfileRepository;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/{userId}/active")
    public ActiveUserResponse active(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        User user = userRepository.findById(userId).orElse(null);
        return new ActiveUserResponse(userId, user != null && user.isEnabled());
    }

    @GetMapping("/{userId}/profile")
    public UserProfileResponse profile(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String displayName = ((user.getFirstName() == null ? "" : user.getFirstName().trim()) + " "
                + (user.getLastName() == null ? "" : user.getLastName().trim())).trim();
        if (displayName.isBlank()) displayName = user.getEmail();
        return new UserProfileResponse(userId, displayName);
    }

    @GetMapping("/{userId}/kyc-status")
    public KycStatusResponse kycStatus(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new KycStatusResponse(userId, "NOT_STARTED");
        }

        KycProfile profile = kycProfileRepository.findByUserId(userId).orElse(null);
        return new KycStatusResponse(userId, profile == null ? "NOT_STARTED" : profile.getStatus());
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || !MessageDigest.isEqual(
                suppliedSecret.getBytes(StandardCharsets.UTF_8),
                internalSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal service secret");
        }
    }

    public record ActiveUserResponse(UUID userId, boolean active) {}
    public record UserProfileResponse(UUID userId, String displayName) {}
    public record KycStatusResponse(UUID userId, String status) {}
}
