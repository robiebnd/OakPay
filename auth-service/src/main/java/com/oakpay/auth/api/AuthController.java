package com.oakpay.auth.api;

import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.service.AuthService;
import com.oakpay.auth.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final TwoFactorService twoFactorService;
    public AuthController(AuthService authService,TwoFactorService twoFactorService){this.authService=authService;this.twoFactorService=twoFactorService;}
    @PostMapping("/register") public ResponseEntity<AuthDtos.RegistrationResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));}
    @PostMapping("/login") public TwoFactorDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request){return authService.login(request);}
    @PostMapping("/verify-2fa") public AuthDtos.TokenResponse verifyTwoFactor(@Valid @RequestBody TwoFactorDtos.LoginVerifyRequest request){return authService.verifyTwoFactorLogin(request);}
    @PostMapping("/refresh") public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request){return authService.refresh(request);}
    @PostMapping("/logout") public ResponseEntity<Void> logout(@Valid @RequestBody AuthDtos.RefreshRequest request){authService.logout(request);return ResponseEntity.noContent().build();}
    @PostMapping("/verify-email") public AuthDtos.VerificationResponse verifyEmail(@Valid @RequestBody AuthDtos.VerifyEmailRequest request){return authService.verifyEmail(request);}
    @PostMapping("/resend-verification") public AuthDtos.VerificationResponse resendVerification(@Valid @RequestBody AuthDtos.ResendVerificationRequest request){return authService.resendVerification(request);}
    @PostMapping("/forgot-password") public AuthDtos.PasswordResetResponse forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request){return authService.forgotPassword(request);}
    @PostMapping("/reset-password") public AuthDtos.PasswordResetResponse resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request){return authService.resetPassword(request);}
    @PostMapping("/change-password") public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,@Valid @RequestBody AuthDtos.ChangePasswordRequest request){authService.changePassword(principal,request);return ResponseEntity.noContent().build();}
}
