package com.oakpay.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class TwoFactorDtos {
    private TwoFactorDtos() {}

    public record StatusResponse(boolean enabled) {}
    public record SetupResponse(String secret, String otpauthUri) {}
    public record CodeRequest(@NotBlank @Size(min=6, max=6) String code) {}
    public record DisableRequest(@NotBlank String password, @NotBlank @Size(min=6, max=6) String code) {}
    public record LoginVerifyRequest(@NotBlank String challengeToken, @NotBlank @Size(min=6, max=6) String code) {}
    public record LoginResponse(String tokenType, String accessToken, String refreshToken, long expiresIn, boolean requiresTwoFactor, String challengeToken) {}
    public record EnabledResponse(boolean enabled, String message) {}
}
