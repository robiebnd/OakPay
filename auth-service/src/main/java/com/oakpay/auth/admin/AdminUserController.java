package com.oakpay.auth.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDtos.UserResponse>> getUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(
                service.getUsers(status, role)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDtos.UserResponse> getUser(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                service.getUser(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserDtos.UserResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody AdminUserDtos.UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(
                service.updateStatus(
                        id,
                        request.status()
                )
        );
    }
}