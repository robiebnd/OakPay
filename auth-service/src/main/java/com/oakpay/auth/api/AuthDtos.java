package com.oakpay.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(@NotBlank @Email @Size(max=320) String email,@NotBlank @Size(min=8,max=72) String password,@NotBlank @Size(max=100) String firstName,@NotBlank @Size(max=100) String lastName) {}
    public record LoginRequest(@NotBlank @Email @Size(max=320) String email,@NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record VerifyEmailRequest(@NotBlank @Email @Size(max=320) String email,@NotBlank @Size(min=6,max=6) String code) {}
    public record ResendVerificationRequest(@NotBlank @Email @Size(max=320) String email) {}
    public record ForgotPasswordRequest(@NotBlank @Email @Size(max=320) String email) {}
    public record ResetPasswordRequest(@NotBlank @Email @Size(max=320) String email,@NotBlank @Size(min=6,max=6) String code,@NotBlank @Size(min=8,max=72) String newPassword) {}
    public record ChangePasswordRequest(@NotBlank String currentPassword,@NotBlank @Size(min=8,max=72) String newPassword) {}
    public record VerificationResponse(String message, boolean verified, String developmentCode) {}
    public record PasswordResetResponse(String message, boolean reset, String developmentCode) {}
    public record RegistrationResponse(UserResponse user, String developmentCode) {}
    public record UserResponse(UUID id,String email,String firstName,String lastName,boolean emailVerified,String phoneNumber,String country,LocalDate dateOfBirth) {}
    public record ProfileUpdateRequest(@NotBlank @Size(max=100) String firstName,@NotBlank @Size(max=100) String lastName,@Size(max=30) String phoneNumber,@Size(max=100) String country,LocalDate dateOfBirth) {}
    public record TokenResponse(String tokenType,String accessToken,String refreshToken,long expiresIn) {}
}
