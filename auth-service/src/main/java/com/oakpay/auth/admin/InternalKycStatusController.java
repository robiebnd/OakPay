package com.oakpay.auth.admin;

import com.oakpay.auth.user.KycProfile;
import com.oakpay.auth.user.KycProfileRepository;
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
public class InternalKycStatusController {
    private final KycProfileRepository kycProfileRepository;
    private final String internalSecret;

    public InternalKycStatusController(
            KycProfileRepository kycProfileRepository,
            @Value("${oakpay.internal-secret:oakpay-internal-development-secret-change-before-production}") String internalSecret) {
        this.kycProfileRepository = kycProfileRepository;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/{userId}/kyc-status")
    public KycStatusResponse status(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        KycProfile profile = kycProfileRepository.findByUserId(userId).orElse(null);
        String status = profile == null || profile.getStatus() == null
                ? "NOT_STARTED"
                : profile.getStatus().trim().toUpperCase();
        return new KycStatusResponse(userId, status);
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || !MessageDigest.isEqual(
                suppliedSecret.getBytes(StandardCharsets.UTF_8),
                internalSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal service secret");
        }
    }

    public record KycStatusResponse(UUID userId, String status) {}
}
