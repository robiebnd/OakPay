package com.oakpay.auth.api;

import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        return new AuthDtos.UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.isEmailVerified());
    }
}
