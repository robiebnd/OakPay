package com.oakpay.auth.api;

import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class MeController {
    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(findUser(principal));
    }

    @PutMapping("/me")
    public ResponseEntity<AuthDtos.UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AuthDtos.ProfileUpdateRequest request) {
        User user = findUser(principal);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        user.setCountry(normalizeOptional(request.country()));
        user.setDateOfBirth(request.dateOfBirth());
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    private User findUser(UserPrincipal principal) {
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }

    private AuthDtos.UserResponse toResponse(User user) {
        return new AuthDtos.UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.isEmailVerified(), user.getPhoneNumber(), user.getCountry(), user.getDateOfBirth());
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
