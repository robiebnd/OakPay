package com.oakpay.auth.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public final class AdminUserDtos {

    private AdminUserDtos() {
    }

    public record UserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String role,
            String status,
            boolean emailVerified,
            LocalDateTime createdAt
    ) {
    }

    public record UpdateStatusRequest(
            String status
    ) {
    }
}