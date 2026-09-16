package com.oakpay.auth.admin;

import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserDtos.UserResponse> getUsers(String status, String role) {
        List<User> users;
        if (status != null && !status.isBlank()) {
            users = userRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else if (role != null && !role.isBlank()) {
            users = userRepository.findByRoleOrderByCreatedAtDesc(role.trim().toUpperCase());
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc();
        }
        return users.stream().map(this::toResponse).toList();
    }

    public AdminUserDtos.UserResponse getUser(UUID id) {
        return toResponse(userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id)));
    }

    public AdminUserDtos.UserResponse updateStatus(UUID id, String status) {
        if (status == null || status.isBlank()) throw new IllegalArgumentException("Account status is required.");
        String normalizedStatus = status.trim().toUpperCase();
        if (!normalizedStatus.equals("ACTIVE") && !normalizedStatus.equals("INACTIVE")) {
            throw new IllegalArgumentException("Account status must be ACTIVE or INACTIVE.");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setStatus(normalizedStatus);
        user.setEnabled(normalizedStatus.equals("ACTIVE"));
        return toResponse(userRepository.save(user));
    }

    public long countActive() { return userRepository.countByStatus("ACTIVE"); }
    public long countInactive() { return userRepository.countByStatus("INACTIVE"); }
    public long countAdmins() { return userRepository.countByRole("ADMIN"); }

    private AdminUserDtos.UserResponse toResponse(User user) {
        return new AdminUserDtos.UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole(), user.getStatus(), user.isEmailVerified(), user.getCreatedAt());
    }
}
