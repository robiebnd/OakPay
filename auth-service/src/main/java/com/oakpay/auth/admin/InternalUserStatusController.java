package com.oakpay.auth.admin;

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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
public class InternalUserStatusController {
    private final UserRepository userRepository;
    private final String internalSecret;

    public InternalUserStatusController(
            UserRepository userRepository,
            @Value("${oakpay.internal-secret:oakpay-internal-development-secret-change-before-production}") String internalSecret) {
        this.userRepository = userRepository;
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

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || !java.security.MessageDigest.isEqual(
                suppliedSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal service secret");
        }
    }

    public record ActiveUserResponse(UUID userId, boolean active) {}
}
