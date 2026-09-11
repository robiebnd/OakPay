package com.oakpay.auth.api;

import com.oakpay.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){this.authService=authService;}
    @PostMapping("/register") public ResponseEntity<AuthDtos.RegistrationResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));}
    @PostMapping("/login") public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request){return authService.login(request);}
    @PostMapping("/refresh") public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request){return authService.refresh(request);}
    @PostMapping("/logout") public ResponseEntity<Void> logout(@Valid @RequestBody AuthDtos.RefreshRequest request){authService.logout(request);return ResponseEntity.noContent().build();}
    @PostMapping("/verify-email") public AuthDtos.VerificationResponse verifyEmail(@Valid @RequestBody AuthDtos.VerifyEmailRequest request){return authService.verifyEmail(request);}
    @PostMapping("/resend-verification") public AuthDtos.VerificationResponse resendVerification(@Valid @RequestBody AuthDtos.ResendVerificationRequest request){return authService.resendVerification(request);}
    @PostMapping("/forgot-password") public AuthDtos.PasswordResetResponse forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request){return authService.forgotPassword(request);}
    @PostMapping("/reset-password") public AuthDtos.PasswordResetResponse resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request){return authService.resetPassword(request);}
}
